/**
 * 
 */
package xyz.javanew.controller;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import xyz.javanew.constant.ResultType;
import xyz.javanew.constant.SysConfig;
import xyz.javanew.domain.Result;
import xyz.javanew.service.CacheService;
import xyz.javanew.util.QrcodeUtil;
import xyz.javanew.util.QrcodeUtil.QrcodeInfo;

/**
 * @Desc
 * @author wewenge.yan
 * @Date 2016年11月25日
 * @ClassName LotterySsqController
 */
/**
 * @author yanwenge
 */
@Controller
@RequestMapping("api")
public class ApiController {
    private static final Logger logger = Logger.getLogger(ApiController.class);
    @Autowired
    private CacheService cacheService;

    @RequestMapping(value = { "qrcode" }, produces = { "application/json;charset=UTF-8" })
    public void qrcode(HttpServletRequest request, HttpServletResponse response, ModelMap map, QrcodeInfo qrcodeInfo) {
        try {
            String uploadAbsolutePath = cacheService.getUploadAbsolutePath();
            qrcodeInfo.setQrcodePath(uploadAbsolutePath);
            String baseUrl = cacheService.getBaseUrl(SysConfig.BASE_URL);
            String logoPath = qrcodeInfo.getLogoPath();
            if (StringUtils.hasText(logoPath)) {
                logoPath =
                        qrcodeInfo.getLogoPath().replace(baseUrl + "/" + cacheService.getUploadFolder(),
                                uploadAbsolutePath);
                qrcodeInfo.setLogoPath(logoPath);
            }
            qrcodeInfo.setQrcodeName("temp-qrcode");
            qrcodeInfo.setBackground(qrcodeInfo.getBackground().replace("#", ""));
            qrcodeInfo.setForeground(qrcodeInfo.getForeground().replace("#", ""));
            String generateQrcode = QrcodeUtil.generateQrcode(qrcodeInfo);
            // 读取文件
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(generateQrcode));
            OutputStream out = response.getOutputStream();
            // 写文件
            byte[] buffer = new byte[1024];
            while (bis.read(buffer) != -1) {
                out.write(buffer);
            }
            bis.close();
            out.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @RequestMapping(value = { "/qrcode/parse" }, produces = { "application/json;charset=UTF-8" })
    public @ResponseBody Result<String> parseQrcode(HttpServletRequest request, ModelMap map, String qrcodeImage) {
        Result<String> result = new Result<String>();
        try {
            String uploadAbsolutePath = cacheService.getUploadAbsolutePath();
            String baseUrl = cacheService.getBaseUrl(SysConfig.BASE_URL);
            if (StringUtils.hasText(qrcodeImage)) {
                qrcodeImage = qrcodeImage.replace(baseUrl + "/" + cacheService.getUploadFolder(), uploadAbsolutePath);
            }
            String qrcodeText = QrcodeUtil.parseQrcode(qrcodeImage);
            result.setData(qrcodeText);
            result.setResultStatusAndMsg(ResultType.SUCCESS, null);
        } catch (Exception e) {
            result.setResultStatusAndMsg(ResultType.SERVICE_ERROR, null);
        }
        return result;
    }
}
