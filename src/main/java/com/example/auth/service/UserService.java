package com.example.auth.service;

import com.example.auth.entity.AppUser;
import com.example.auth.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//    This is a java class provided to us by the dependancy we added for sending mail with java
    private final JavaMailSender mailSender;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
//      adding the mailSender to our dependancy injection in the constructor
        this.mailSender = mailSender;
    }

    public AppUser saveUser(AppUser user) throws MessagingException {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
//        This will generate the token for us using the UUID and save it in the user profile
        user.setVerificationToken(UUID.randomUUID().toString()); // Generate random token

        AppUser savedUser = userRepository.save(user);

        // Calling the sendVerificationEmail method which is configured to send the email
        sendVerificationEmail(savedUser);

        return savedUser;
    }

//    This method is used to define the sender, reciever, and the body of the
//    MessagingException is an exception related to email messaging
    private void sendVerificationEmail(AppUser user) throws MessagingException {
//        Mime stands for Multipurpose Internet Mail Extensions
        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        Here we are setting the properties for our emailing and
//        first argument (false) is referring to if we want to send a miltiPart email
//        second argument (utf-8) is referring to the encoding for the message
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
//        We are storing the url that will send the user to the verify endpoint with the token
        String verifyUrl = "http://localhost:8080/api/verify?token=" + user.getVerificationToken(); // Replace with actual URL
//        This is the body of the message which we can write using html. the important part is to include a <a> tag with the token in the href attribute
        String htmlMsg = "<h3>Verify your email address</h3><br><a href=\"" + verifyUrl + "\">Click here to verify</a>";
        mimeMessage.setContent(htmlMsg, "text/html");
//        Using the getting to get the email of the user we want to email
        helper.setTo(user.getEmail());
//        Subject line of the email
        helper.setSubject("Email Verification");
//        Sends the email
        mailSender.send(mimeMessage);
    }





    public AppUser findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}