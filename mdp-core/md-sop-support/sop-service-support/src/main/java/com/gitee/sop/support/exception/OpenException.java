package com.gitee.sop.support.exception;

import com.gitee.sop.support.message.I18nMessage;
import com.gitee.sop.support.message.OpenMessage;
import com.gitee.sop.support.message.OpenMessageFactory;
import lombok.Getter;

import java.util.Locale;

/**
 * @author 六如
 */
@Getter
public class OpenException extends RuntimeException {
    private static final long serialVersionUID = -8059687941272829442L;

    private final String subCode;

    private final String subMsg;

    private final String solution;

    public OpenException(I18nMessage i18nMessage, Locale locale, Object... params) {
        this.subCode = i18nMessage.getConfigKey();
        OpenMessage openMessage = OpenMessageFactory.getMessage(i18nMessage, locale, params);
        this.subMsg = openMessage.getMsg();
        this.solution = openMessage.getSolution();
    }

    public OpenException(I18nMessage openError, Object... params) {
        this(openError, Locale.ENGLISH, params);
    }

    public OpenException(String subCode, String subMsg, String solution) {
        this.subCode = subCode;
        this.subMsg = subMsg;
        this.solution = solution;
    }

    public OpenException(String subCode, String subMsg) {
        this(subCode, subMsg, "");
    }

    @Override
    public String toString() {
        return "<OPEN_ERROR>" + String.join("@@", subCode, subMsg, solution) + "</OPEN_ERROR>";
    }
}
