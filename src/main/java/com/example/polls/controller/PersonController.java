package com.example.polls.controller;

import com.example.polls.model.BarCodeNumber;
import com.example.polls.model.HistorycalData;
import com.example.polls.model.Person;
import com.example.polls.model.dto.PersonDTO;
import com.example.polls.payload.ApiResponse;
import com.example.polls.repository.BarCodeNumberRepository;
import com.example.polls.repository.CategoryRepository;
import com.example.polls.repository.HistorycalDataRepository;
import com.example.polls.repository.PersonRepository;
import com.example.polls.repository.specs.PersonSpecification;
import com.example.polls.repository.specs.SearchCriteria;
import com.example.polls.repository.specs.SearchOperation;
import com.example.polls.service.PdfService;
import com.example.polls.service.PdfService2;
import com.example.polls.service.PersonService;
import com.example.polls.util.Utils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/person/")
public class PersonController {
    private PersonService personService;
    private PdfService pdfService;


    @Autowired
    public void setPdfService(PdfService pdfService) {
        this.pdfService = pdfService;
    }


    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }



    @GetMapping(value = "/personData")
    public List<PersonDTO> getLoadedPersonList() {
        return personService.getLoadedPersonList();
    }

    @GetMapping(value = "/getDataInQueue")
    public List<PersonDTO> getDataInQueue() {
        return personService.getPersonInQueue();
    }

    @PostMapping(value = "/addPerson", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String addPerson(@RequestParam(value = "image", required = false) MultipartFile file,
                            @RequestParam(value = "secondName", required = false) String secondName,
                            @RequestParam(value = "firstName", required = false) String firstName,
                            @RequestParam(value = "iin", required = false) String iin,
                            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                            @RequestParam(value = "price", required = false) Long price,
                            @RequestParam(value = "category", required = false) String category,
                            @RequestParam(value = "categoryId", required = false) Long cardId) throws IOException {

        if(file == null && secondName == null && firstName == null && iin == null
        && phoneNumber == null && price == null && category == null && cardId == null)
            throw new IllegalArgumentException("Все поля обязательны для заполнения");

        return personService.addPerson(file, secondName,
                firstName, iin, phoneNumber, price, category, cardId);
    }

    @GetMapping(value = "/sendToNormalize")
    public String sendToNormalize(@RequestParam("ids") List<Long> list) {

       return personService.sendToNormalize(list);
    }

//    @PostMapping(value = "/generatePdf", produces = "application/zip")
//    public ResponseEntity<Resource> serveFile(/*@RequestParam("ids") List<Long> list*/) throws IOException, DocumentException {
//            File file = new File("zip/edited.zip");
//            file.delete();
//
//        pdfService.getZippedFile();
//        HttpHeaders headers=new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + "edited.zip" + "\"");
//        headers.add("Access-Control-Expose-Headers",HttpHeaders.CONTENT_DISPOSITION + "," + HttpHeaders.CONTENT_LENGTH);
//        Path path = Paths.get("zip").resolve("edited.zip");
//        Resource resource = new UrlResource(path.toUri());
//
////        if (resource.exists()) {
////            for(long id : list) {
////                Optional<Person> person = personRepository.findById(id);
////                if(person.isPresent()) {
////                    HistorycalData historycalData = new HistorycalData();
////                    historycalData.setBarCode(person.get().getBarCode());
////                    historycalData.setCardType(person.get().getCardType());
////                    historycalData.setFirstName(person.get().getFirstName());
////                    historycalData.setSecondName(person.get().getSecondName());
////                    historycalData.setIin(person.get().getIin());
////
////                    historycalDataRepository.save(historycalData);
////                    personRepository.update(id, "Обработан");
////                }
////            }
//            return ResponseEntity.ok().headers(headers).body(resource);
//        }

    @PostMapping(value = "/generatePdf", produces = "application/zip")
    public ResponseEntity<Resource> generatePdf(/*@RequestParam("ids") List<Long> list*/) throws IOException, DocumentException {

       return personService.generatePdf();
    }


    @GetMapping(value = "/search")
    public List<PersonDTO> search(@RequestParam("key") String key, @RequestParam("value") String value) {
        return personService.searchPerson(key, value);
    }


    @GetMapping(value = "/deleteById", produces = "application/json")
    public ResponseEntity<?> search(@RequestParam("id") Long id) {
        return personService.deleteById(id);
    }

    @GetMapping(value = "/editPerson", produces = "application/json")
    public ResponseEntity<?> edit(@RequestParam("id") Long id,
                                  @RequestParam("firstName") String firstName,
                                  @RequestParam("secondName") String secondName,
                                  @RequestParam("iin") String iin,
                                  @RequestParam("phoneNumber") String phoneNumber/*,
                                  @RequestParam("category") String category,
                                  @RequestParam("categoryId") Long categoryId,
                                  @RequestParam("price") Long price*/) {

        return personService.editPerson(id, firstName, secondName, iin, phoneNumber/*, category, categoryId, price*/);
    }

    @GetMapping(value = "/sendToEdit", produces = "application/json")
    public ResponseEntity<?> sendToEdit(@RequestParam("id") Long id) {
        return personService.sendToReEdit(id);
    }

}
