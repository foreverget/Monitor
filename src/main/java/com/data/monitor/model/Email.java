package com.data.monitor.model;

import com.data.monitor.utils.FileUtil;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.List;

/**
 * Created by Victor on 16-12-8.
 */
public class Email {
    private static final Logger logger = Logger.getLogger(Email.class);
    private List<String> to;
    private String from;
    private String subject;
    private String content;

    public Email(List<String> to, String from, String subject, String content) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.content = content;
    }

    public String listToString(List<String> toList) {
        if (toList == null) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        boolean flag = false;
        for (String to : toList) {
            if (flag) {
                result.append(",");
            } else {
                flag = true;
            }
            result.append(to);
        }
        return result.toString();
    }

    public void callShell(String shellString) {
        String[] command = {"/bin/sh", "-c", shellString};
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = input.readLine()) != null) {
                line += line;    //获取命令执行结果
            }
            input.close();
            int exitValue = process.waitFor();    //获取命令执行返回状态码
            if (0 != exitValue) {

                System.out.println("call shell failed. error code is :" + exitValue);
                System.out.println("Reason: " + line);
                logger.info("发送失败");
            } else {
                logger.info("发送成功");
            }
        } catch (Throwable e) {
            System.out.println("call shell failed. " + e);
        }
    }

    public void send(String tempPath) throws Exception {
        String mailMessage =
                "SUBJECT: {0} \n"    //邮件标题
                        + "TO: {1} \n"       //收件人
                        + "MIME-VERSION: 1.0 \n"
                        + "Content-type: text/html;charset=UTF-8 \n"
                        + "{2} \n";
        String receiver = listToString(this.to);
        mailMessage = MessageFormat.format(mailMessage, this.subject, receiver, this.content);
        System.out.println(this.content);
        FileUtil.writeTXTtoFile(mailMessage, tempPath);
        String mailCommand = "cat " + tempPath + " | sendmail -t";
        callShell(mailCommand);
        FileUtil.deleteFile(tempPath);
    }
}
