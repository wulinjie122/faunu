package com.zengdaimoney.faunu.internals;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

/**
 * 邮件处理Handler
 *
 * @author wulj
 */
@Slf4j
@Component
public class MailHandler {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${faunu.project}")
    private String projectName;

    public void sendSimpleEmail(String[] receivers, String subject, String content) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(receivers);
        message.setSubject("【"+ projectName + "】" + subject);
        message.setText(content);

        mailSender.send(message);

    }

    public void sendHtmlEmail(String[] receivers, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        //true表示需要创建一个multipart message
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(sender);
        helper.setTo(receivers);
        helper.setSubject("【"+ projectName + "】" + subject);
        helper.setText(content, true);

        mailSender.send(message);
    }

    /**
     * 发送带附件的邮件
     *
     * @param receivers
     * @param subject
     * @param content
     * @param filePath
     * @throws MessagingException
     */
    public void sendAttachmentsEmail(String[] receivers, String subject, String content, String filePath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(sender);
        helper.setTo(receivers);
        helper.setSubject("【"+ projectName + "】" + subject);
        // true表示这个邮件是有附件的
        helper.setText(content, true);

        //创建文件系统资源
        FileSystemResource file = new FileSystemResource(new File(filePath));
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);

        //添加附件
        helper.addAttachment(fileName, file);

        mailSender.send(message);
    }

    /**
     * 内联资源邮件
     *
     * @param receivers
     * @param subject
     * @param content
     * @param rscPath
     * @param rscId
     */
    public void sendInlineResourceEmail(String[] receivers, String subject, String content, String rscPath, String rscId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(sender);
        helper.setTo(receivers);
        helper.setSubject("【"+ projectName + "】" + subject);
        helper.setText(content, true);

        FileSystemResource res = new FileSystemResource(new File(rscPath));

        //添加内联资源，一个id对应一个资源，最终通过id来找到该资源
        //添加多个图片可以使用多条 <img src='cid:" + rscId + "' > 和 helper.addInline(rscId, res) 来实现
        helper.addInline(rscId, res);

        mailSender.send(message);
    }

}
