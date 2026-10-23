package top.mddata.open.service.notify.pipeline;

import com.gitee.sop.support.exception.OpenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.open.service.notify.NotifyProperties;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * 回调地址安全校验：notifyUrl 由开发者自助配置，投递前必须防 SSRF。
 * 仅允许 http/https，且解析后的所有 IP 均不得为内网/回环/链路本地地址。
 * mdp.notify.allowed-hosts 白名单内的 host 直接放行（仅用于本地开发联调，生产应保持为空）。
 * 校验基于配置时/投递前的 DNS 解析，不防 DNS rebinding；如需更高保障应在投递层固定解析结果。
 */
@Component
@RequiredArgsConstructor
public class NotifyUrlValidator {

    private static final String ERR_CODE = "NOTIFY_URL_INVALID";

    private final NotifyProperties notifyProperties;

    public void validate(String notifyUrl) {
        if (notifyUrl == null || notifyUrl.isBlank()) {
            throw new OpenException(ERR_CODE, "回调地址不能为空");
        }
        URI uri;
        try {
            uri = URI.create(notifyUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new OpenException(ERR_CODE, "回调地址格式非法：" + notifyUrl);
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new OpenException(ERR_CODE, "回调地址仅支持 http/https 协议：" + notifyUrl);
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new OpenException(ERR_CODE, "回调地址缺少主机名：" + notifyUrl);
        }
        // 白名单内的 host（如本地开发联调的 127.0.0.1/localhost）直接放行，不再做内网拦截
        if (isAllowedHost(host)) {
            return;
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new OpenException(ERR_CODE, "回调地址主机无法解析：" + host);
        }
        for (InetAddress address : addresses) {
            InetAddress effective = unwrapIPv4Mapped(address);
            if (effective.isLoopbackAddress() || effective.isSiteLocalAddress()
                    || effective.isAnyLocalAddress() || effective.isLinkLocalAddress()
                    || effective.isMulticastAddress() || isUniqueLocalIPv6(effective)) {
                throw new OpenException(ERR_CODE, "回调地址不允许指向内网地址：" + host);
            }
        }
    }

    /** host 精确匹配白名单（忽略大小写） */
    private boolean isAllowedHost(String host) {
        return notifyProperties.getAllowedHosts().stream().anyMatch(allowed -> allowed.equalsIgnoreCase(host));
    }

    /** 还原 IPv4-mapped/compatible IPv6 地址（::ffff:127.0.0.1 等），防止用 IPv6 形式绕过内网校验 */
    private static InetAddress unwrapIPv4Mapped(InetAddress address) {
        byte[] bytes = address.getAddress();
        if (bytes.length != 16) {
            return address;
        }
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return address;
            }
        }
        boolean mapped = bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff;
        boolean compatible = bytes[10] == 0 && bytes[11] == 0;
        if (!mapped && !compatible) {
            return address;
        }
        // ::1 与 :: 不还原（已被 isLoopback/isAnyLocal 覆盖）
        if (bytes[12] == 0 && bytes[13] == 0 && bytes[14] == 0 && (bytes[15] == 0 || bytes[15] == 1)) {
            return address;
        }
        try {
            return InetAddress.getByAddress(new byte[]{bytes[12], bytes[13], bytes[14], bytes[15]});
        } catch (UnknownHostException e) {
            return address;
        }
    }

    /** IPv6 ULA fc00::/7（唯一本地地址，属内网） */
    private static boolean isUniqueLocalIPv6(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    }
}
