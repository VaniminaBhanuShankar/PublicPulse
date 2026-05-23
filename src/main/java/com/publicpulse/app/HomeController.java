package com.publicpulse.app;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.publicpulse.app.model.Complaint;
import com.publicpulse.app.model.User;
import com.publicpulse.app.repository.ComplaintRepository;
import com.publicpulse.app.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    // HOME PAGE

    @GetMapping("/")
    public String home(

            HttpSession session,

            Model model

    ) {

        String userName =
                (String) session.getAttribute(
                        "loggedUser"
                );

        if(userName != null){

            int pendingCount = 0;

            for(Complaint complaint :
                    complaintRepository
                    .findByUserNameOrderByIdDesc(
                            userName
                    )){

                if(complaint.getStatus()
                        .equals("Pending")){

                    pendingCount++;

                }

            }

            model.addAttribute(
                    "pendingCount",
                    pendingCount
            );

        }

        return "index";
    }

    // HELP PAGE

    @GetMapping("/help")
    public String helpPage() {

        return "help";
    }

    // ADMIN LOGIN PAGE

    @GetMapping("/adminlogin")
    public String adminLoginPage(){

        return "adminlogin";
    }

    // ADMIN LOGIN

    @PostMapping("/adminlogin")
    public String adminLogin(

            @RequestParam String username,

            @RequestParam String password,

            HttpSession session,

            Model model

    ){

        if(username.equals("Bhanu")
        && password.equals("Bhanu123")){

            session.setAttribute(
                    "admin",
                    true
            );

            return "redirect:/admin";
        }

        model.addAttribute(
                "error",
                true
        );

        return "adminlogin";
    }

    // ADMIN LOGOUT

    @GetMapping("/adminlogout")
    public String adminLogout(

            HttpSession session

    ){

        session.invalidate();

        return "redirect:/adminlogin";
    }

    // MY COMPLAINTS PAGE

    @GetMapping("/mycomplaints")
    public String myComplaints(

            HttpSession session,

            Model model

    ) {

        // LOGIN CHECK

        if(session.getAttribute("loggedUser") == null){

            return "redirect:/login";
        }

        String userName =
                (String) session.getAttribute(
                        "loggedUser"
                );

        model.addAttribute(
                "complaints",
                complaintRepository
                .findByUserNameOrderByIdDesc(
                        userName
                )
        );

        return "mycomplaints";
    }

    // ADMIN PAGE

    @GetMapping("/admin")
    public String adminPage(

            HttpSession session,

            Model model

    ) {

        if(session.getAttribute("admin") == null){

            return "redirect:/adminlogin";

        }

        model.addAttribute(
                "complaints",
                complaintRepository.findAllByOrderByIdDesc()
        );

        return "admin";
    }

    // UPDATE STATUS

    @PostMapping("/updateStatus")
    public String updateStatus(

            @RequestParam Long id,

            @RequestParam String status

    ){

        Complaint complaint =
                complaintRepository.findById(id)
                .orElse(null);

        if(complaint != null){

            complaint.setStatus(status);

            complaintRepository.save(complaint);

        }

        return "redirect:/admin";
    }

    // DELETE COMPLAINT

    @GetMapping("/deleteComplaint/{id}")
    public String deleteComplaint(

            @PathVariable Long id

    ){

        complaintRepository.deleteById(id);

        return "redirect:/admin";
    }

    // SIGNUP PAGE

    @GetMapping("/signup")
    public String signupPage() {

        return "signup";
    }

    // LOGIN PAGE

    @GetMapping("/login")
    public String loginPage() {

        return "login";
    }

    // FORGOT PASSWORD PAGE

    @GetMapping("/forgot")
    public String forgotPage() {

        return "forgot";
    }

    // SAVE USER

    @PostMapping("/signup")
    public String signupUser(

            @RequestParam String name,

            @RequestParam String email,

            @RequestParam String mobile,

            @RequestParam String password

    ) {

        User user = new User();

        user.setName(name);

        user.setEmail(email);

        user.setMobile(mobile);

        user.setPassword(password);

        userRepository.save(user);

        return "redirect:/login";
    }

    // LOGIN USER WITH SESSION

    @PostMapping("/login")
    public String loginUser(

            @RequestParam String username,

            @RequestParam String password,

            HttpSession session,

            Model model

    ) {

        User user =
                userRepository.findByEmail(username);

        if(user == null){

            user =
                    userRepository.findByMobile(username);

        }

        if(user != null &&
                user.getPassword().equals(password)){

            // SAVE USER SESSION

            session.setAttribute(
                    "loggedUser",
                    user.getName()
            );

            return "redirect:/";
        }

        model.addAttribute(
                "error",
                "Invalid email/mobile or password"
        );

        return "login";
    }

    // USER LOGOUT

    @GetMapping("/logout")
    public String logout(

            HttpSession session

    ){

        session.invalidate();

        return "redirect:/login";
    }

    // FORGOT PASSWORD

    @PostMapping("/forgot-password")
    public String forgotPassword(

            @RequestParam String email,

            @RequestParam String password,

            Model model

    ) {

        User user =
                userRepository.findByEmail(email);

        if(user != null){

            user.setPassword(password);

            userRepository.save(user);

            model.addAttribute(
                    "message",
                    "Password updated successfully!"
            );

        }
        else{

            model.addAttribute(
                    "message",
                    "Email not found!"
            );

        }

        return "forgot";
    }

    // SUBMIT COMPLAINT

    @PostMapping("/submit")
    public String submitComplaint(

            HttpSession session,

            @RequestParam String title,

            @RequestParam(required = false)
            String description,

            @RequestParam String category,

            @RequestParam String location,

            @RequestParam("imageFile")
            MultipartFile imageFile

    ) throws IOException {

        // LOGIN CHECK

        if(session.getAttribute("loggedUser") == null){

            return "redirect:/login";
        }

        // UPLOADS FOLDER

        String uploadDir =
                System.getProperty("user.dir")
                + "/uploads/";

        File folder =
                new File(uploadDir);

        if(!folder.exists()){

            folder.mkdirs();

        }

        // UNIQUE FILE NAME

        String fileName =
                UUID.randomUUID() + "_" +
                imageFile.getOriginalFilename();

        // SAVE IMAGE

        imageFile.transferTo(
                new File(uploadDir + fileName)
        );

        // SAVE COMPLAINT

        Complaint complaint =
                new Complaint();

        complaint.setTitle(title);

        complaint.setDescription(description);

        complaint.setCategory(category);

        complaint.setLocation(location);

        complaint.setStatus("Pending");

        complaint.setImageName(fileName);

        // SAVE USER NAME

        complaint.setUserName(

                (String) session.getAttribute(
                        "loggedUser"
                )

        );

        complaint.setCreatedAt(LocalDateTime.now());

        complaintRepository.save(complaint);

        // SUCCESS MESSAGE

        return "redirect:/?success";
    }

}