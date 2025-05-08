package com.siemens.internship;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Change:
    // Added validation: name must not be blank
    @NotBlank(message = "Name must not be blank")
    private String name;

    // Change:
    // Added validation: description must not exceed 255 characters
    @Size(max = 255, message = "Description must be smaller than 255 characters")
    private String description;

    private String status;

    // Change:
    // Added validation: email must not be blank and must be a valid format
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;
}