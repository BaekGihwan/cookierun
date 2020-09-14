package cookierunrun;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Email {
   private Register register;
   private String code;

   public String getCode() {
      return code;
   }

   public Email(Register register) {
      this.register = register;

      int randomNum1 = (int) (Math.random() * 9) + 1;
      int randomNum2 = (int) (Math.random() * 9) + 1;
      int randomNum3 = (int) (Math.random() * 9) + 1;
      int randomNum4 = (int) (Math.random() * 9) + 1;
      int randomNum5 = (int) (Math.random() * 9) + 1;
      int randomNum6 = (int) (Math.random() * 9) + 1;

      code = new String("" + randomNum1 + randomNum2 + randomNum3 + randomNum4 + randomNum5 + randomNum6);

      String userMail = "cokierunproject@gmail.com";
      String password = "yawtllrxwykvtebg";

      Properties prop = new Properties();
      prop.put("mail.smtp.host", "smtp.gmail.com");
      prop.put("mail.smtp.port", 465);
      prop.put("mail.smtp.auth", "true");
      prop.put("mail.smtp.ssl.enable", "true");
      prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

      Session session = Session.getDefaultInstance(prop, new javax.mail.Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userMail, password);
         }
      });
      MimeMessage message = new MimeMessage(session);
      try {
         message.setFrom(new InternetAddress(userMail));
         // user email 넣는다
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(register.getEmailT().getText()));       
         // 메일 제목
         message.setSubject("CookieRun!! 인증번호");
         // 메일 내용을 추가해준다
         message.setText("어서오세요!!\n인증 번호: " + code);
         // 메일 전송
         System.out.println("인증번호" + code);
         Transport.send(message);
      } catch (AddressException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (MessagingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}