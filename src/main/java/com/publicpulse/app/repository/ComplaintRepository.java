package com.publicpulse.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.publicpulse.app.model.Complaint;

public interface ComplaintRepository
extends JpaRepository<Complaint, Long> {

    // LATEST FIRST

    List<Complaint>
    findAllByOrderByIdDesc();

    // USER COMPLAINTS

    List<Complaint>
    findByUserNameOrderByIdDesc(
            String userName
    );

}