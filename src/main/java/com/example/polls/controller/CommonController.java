package com.example.polls.controller;

import com.example.polls.model.Category;
import com.example.polls.model.HistorycalData;
import com.example.polls.model.Person;
import com.example.polls.repository.CategoryRepository;
import com.example.polls.repository.HistorycalDataRepository;
import com.example.polls.repository.PersonRepository;
import com.example.polls.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CommonController {

    private HistorycalDataRepository historycalDataRepository;
    private CategoryRepository categoryRepository;

    @Autowired
    public void setHistorycalDataRepository(HistorycalDataRepository historycalDataRepository) {
        this.historycalDataRepository = historycalDataRepository;
    }


    @Autowired
    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    @GetMapping(value = "/getCategory")
    public List<Category> getCategory() {
        return categoryRepository.findAll();
    }

    @GetMapping(value = "/getHistoryData")
    public List<HistorycalData> getHistorycalData() {
        return historycalDataRepository.findAllByOrderByIdDesc();
    }



}
