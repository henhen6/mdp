package top.mddata.sdk.simple.request.demo;

/**
 * @author 六如
 */
public class DemoFileUploadRequest {
    private String productName;
    private String addTime;

    public String getProductName() {
        return productName;
    }

    public DemoFileUploadRequest setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public String getAddTime() {
        return addTime;
    }

    public DemoFileUploadRequest setAddTime(String addTime) {
        this.addTime = addTime;
        return this;
    }
}
