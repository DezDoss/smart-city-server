package com.example.polls.controller;

import com.example.polls.model.BarCodeNumber;
import com.example.polls.model.HistorycalData;
import com.example.polls.model.Person;
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
    private HistorycalDataRepository historycalDataRepository;
    private BarCodeNumberRepository barCodeNumberRepository;
    private PersonRepository personRepository;
    private CategoryRepository categoryRepository;
    private PdfService pdfService;

    @Autowired
    public void setHistorycalDataRepository(HistorycalDataRepository historycalDataRepository) {
        this.historycalDataRepository = historycalDataRepository;
    }

    @Autowired
    public void setPersonRepository(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Autowired
    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Autowired
    public void setPdfService(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @Autowired
    public void setBarCodeNumberRepository(BarCodeNumberRepository barCodeNumberRepository) {
        this.barCodeNumberRepository = barCodeNumberRepository;
    }

    @GetMapping(value = "/personData")
    public List<Person> getPersonData() {
        List<String> list = new ArrayList<>();
        list.add("Редактирование");
        list.add("Загружен");


        return personRepository.findAllByStatusIn(list);
    }

    @GetMapping(value = "/getDataInQueue")
    public List<Person> getDataInQueue() {
        List<String> list = new ArrayList<>();
        list.add("Отправлен в обработку");
        return personRepository.findAllByStatusIn(list);
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
        log.info("======Adding Person=========");
        log.info("Person firstName : {}", firstName);
        log.info("Person secondName : {}", secondName);
        log.info("Person iin : {}", iin);
        log.info("Person phoneNumber : {}", phoneNumber);
        log.info("Person price : {}", price);
        log.info("Person category : {}", category);
        log.info("Person categoryId : {}", cardId);
        log.info("======Added Person=========");
        Person person = new Person();
        person.setFirstName(firstName);
        person.setSecondName(secondName);
        person.setIin(iin);
        person.setPhoneNumber(phoneNumber);
        person.setCardType(category);
        person.setCardId(cardId);
        person.setPrice(price);
        person.setImage(file.getBytes());
        person.setStatus("Загружен");
        Long barCodeNumber = getBarCodeNumber(cardId.intValue());
        person.setBarCode(barCodeNumber.toString());
        person.setCreatedDate(new Date());

        personRepository.save(person);

        return "Успешно загружен";
    }

    @GetMapping(value = "/sendToNormalize")
    public String sendToNormalize(@RequestParam("ids") List<Long> list) {
        List<Person> personList = new ArrayList<>();
        for(Long id : list) {
           personRepository.update(id, "Отправлен в обработку");
        }
        return "Отправлен в обработку";
    }

    @PostMapping(value = "/generatePdf", produces = "application/zip")
    public ResponseEntity<Resource> serveFile(@RequestParam("ids") List<Long> list) throws IOException, DocumentException {
            File file = new File("zip/edited.zip");
            file.delete();

        pdfService.getZippedFile(list);
        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + "edited.zip" + "\"");
        headers.add("Access-Control-Expose-Headers",HttpHeaders.CONTENT_DISPOSITION + "," + HttpHeaders.CONTENT_LENGTH);
        Path path = Paths.get("zip").resolve("edited.zip");
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists()) {
            for(long id : list) {
                Optional<Person> person = personRepository.findById(id);
                if(person.isPresent()) {
                    HistorycalData historycalData = new HistorycalData();
                    historycalData.setBarCode(person.get().getBarCode());
                    historycalData.setCardType(person.get().getCardType());
                    historycalData.setFirstName(person.get().getFirstName());
                    historycalData.setSecondName(person.get().getSecondName());
                    historycalData.setIin(person.get().getIin());

                    historycalDataRepository.save(historycalData);
                    personRepository.update(id, "Обработан");
                }
            }
            return ResponseEntity.ok().headers(headers).body(resource);
        }
        else return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/search")
    public List<Person> search(@RequestParam("key") String key, @RequestParam("value") String value) {
        PersonSpecification personSpecification = new PersonSpecification();
        personSpecification.add(new SearchCriteria(key, value, SearchOperation.EQUAL));

        List<Person> personList = personRepository.findAll(personSpecification);

        return personList;
    }


    @GetMapping(value = "/deleteById", produces = "application/json")
    public ResponseEntity<?> search(@RequestParam("id") Long id) {
        personRepository.deleteById(id);

       return ResponseEntity.ok()
                .body(new ApiResponse(true, "Успешно удален"));
    }

    @GetMapping(value = "/editPerson", produces = "application/json")
    public ResponseEntity<?> edit(@RequestParam("id") Long id,
                                  @RequestParam("firstName") String firstName,
                                  @RequestParam("secondName") String secondName,
                                  @RequestParam("iin") String iin,
                                  @RequestParam("phoneNumber") String phoneNumber,
                                  @RequestParam("category") String category,
                                  @RequestParam("categoryId") Long categoryId,
                                  @RequestParam("price") Long price) {

        log.info("==========Editing Person by Id = {}", id);
        log.info("Person firstName : {}", firstName);
        log.info("Person secondName : {}", secondName);
        log.info("Person iin : {}", iin);
        log.info("Person phoneNumber : {}", phoneNumber);
        log.info("Person price : {}", price);
        log.info("Person category : {}", category);
        log.info("Person categoryId : {}", categoryId);
        Optional<Person> person = personRepository.findById(id);
        if(person.isPresent()) {
            person.get().setFirstName(firstName);
            person.get().setSecondName(secondName);
            person.get().setIin(iin);
            person.get().setPhoneNumber(phoneNumber);
            person.get().setCardType(category);
            person.get().setCardId(categoryId);
            person.get().setPrice(price);
            log.info("==========Edited Person by Id = {}", id);
            personRepository.save(person.get());
        } else {
            return ResponseEntity.ok()
                    .body(new ApiResponse(false, "Не обновлен"));
        }
        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Успешно обновлен"));
    }

    @GetMapping(value = "/sendToEdit", produces = "application/json")
    public ResponseEntity<?> sendToEdit(@RequestParam("id") Long id) {
        personRepository.update(id, "Редактирование");

        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Успешно отправлен"));
    }


    @GetMapping(value = "/getBarCode")
    private long getBarCodeNumber(@RequestParam("id") int cardId) {
        int id = defineCategory(cardId);
        Optional<BarCodeNumber> list = barCodeNumberRepository.findByCardId(id);
        long barCodeNumber = -1;
        if(list.isPresent()) {
            barCodeNumber = list.get().getBarCodeNumber();
            log.info("before barCode with id = {}: {}",id, barCodeNumber);
            barCodeNumberRepository.update(barCodeNumber + 1l, id);
            log.info("after barCode with id = {}: {}", id,barCodeNumberRepository.findByCardId(id).get().getBarCodeNumber());
        }

        return barCodeNumber;
    }

    private int defineCategory(int id) {
        if(id < 5) {
            return id;
        } else
            return 5;
    }
}
