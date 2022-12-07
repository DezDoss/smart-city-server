package com.example.polls.service;

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
import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersonService {

    private ModelMapper modelMapper;
    private HistorycalDataRepository historycalDataRepository;
    private BarCodeNumberRepository barCodeNumberRepository;
    private PersonRepository personRepository;
    private CategoryRepository categoryRepository;
    private PersonService personService;
    private PdfService3 pdfService3;
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


    @Autowired
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Autowired
    public void setModelMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Autowired
    public void setPdfService3(PdfService3 pdfService3) {
        this.pdfService3 = pdfService3;
    }

    public List<PersonDTO> getLoadedPersonList() {
        List<String> list = new ArrayList<>();
        list.add("Редактирование");
        list.add("Загружен");
        List<Person> personList = personRepository.findAllByStatusIn(list);

        List<PersonDTO> personDTOList = mapList(personList, PersonDTO.class);
        return personDTOList;
    }

    public List<PersonDTO> getPersonInQueue() {
        List<String> list = new ArrayList<>();
        list.add("Отправлен в обработку");

        List<Person> personList = personRepository.findAllByStatusIn(list);

        List<PersonDTO> personDTOList = mapList(personList, PersonDTO.class);
        return personDTOList;
    }

    public String addPerson(MultipartFile file, String secondName,
                            String firstName, String iin,
                            String phoneNumber, Long price,
                            String category, Long cardId) throws IOException {
        log.info("======Adding Person=========");
        log.info("Person firstName : {}", firstName);
        log.info("Person secondName : {}", secondName);
        log.info("Person iin : {}", iin);
        log.info("Person phoneNumber : {}", phoneNumber);
        log.info("Person price : {}", price);
        log.info("Person category : {}", category);
        log.info("Person categoryId : {}", cardId);

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
        log.info("======Added Person=========");
        return "Успешно загружен";
    }

    public String sendToNormalize(List<Long> idList) {
        for (Long id : idList) {
            personRepository.update(id, "Отправлен в обработку");
        }
        return "Отправлен в обработку";
    }

    public ResponseEntity<Resource> generatePdf() throws IOException, DocumentException {

        File file = new File("zip/edited.zip");
        file.delete();
        log.info("Started generating pdf with date = {}", new Date());
        List<Long> idList = pdfService3.getZippedFile();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "edited.zip" + "\"");
        headers.add("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION + "," + HttpHeaders.CONTENT_LENGTH);
        Path path = Paths.get("zip").resolve("edited.zip");
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists()) {
            for (long id : idList) {
                Optional<Person> person = personRepository.findById(id);
                if (person.isPresent()) {
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
            log.info("Finished generating pdf with ids =  {}, date = {}", idList.toString(), new Date());
            return ResponseEntity.ok().headers(headers).body(resource);
        } else
            return ResponseEntity.notFound().build();
    }

    public ResponseEntity<?> deleteById(Long id) {
        personRepository.deleteById(id);

        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Успешно удален"));
    }

    public ResponseEntity<?> editPerson(Long id, String firstName,
                                        String secondName, String iin,
                                        String phoneNumber/*, String category,
                                        Long categoryId, Long price*/) {
        log.info("==========Editing Person by Id = {}", id);
        log.info("Person firstName : {}", firstName);
        log.info("Person secondName : {}", secondName);
        log.info("Person iin : {}", iin);
        /*log.info("Person phoneNumber : {}", phoneNumber);
        log.info("Person price : {}", price);
        log.info("Person category : {}", category);
        log.info("Person categoryId : {}", categoryId);*/
        Optional<Person> person = personRepository.findById(id);
        if(person.isPresent()) {
            person.get().setFirstName(firstName);
            person.get().setSecondName(secondName);
            person.get().setIin(iin);
            person.get().setPhoneNumber(phoneNumber);
            /*person.get().setCardType(category);
            person.get().setCardId(categoryId);
            person.get().setPrice(price);*/
            log.info("==========Edited Person by Id = {}", id);
            personRepository.save(person.get());
        } else {
            return ResponseEntity.ok()
                    .body(new ApiResponse(false, "Не обновлен"));
        }
        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Успешно обновлен"));

    }

    public ResponseEntity<?> sendToReEdit(Long id) {
        personRepository.update(id, "Редактирование");

        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Успешно отправлен"));
    }


    public List<PersonDTO> searchPerson(String key, String value) {
        PersonSpecification personSpecification = new PersonSpecification();
        personSpecification.add(new SearchCriteria(key, value, SearchOperation.EQUAL));

        List<Person> personList = personRepository.findAll(personSpecification);
        List<PersonDTO> personDTOList = mapList(personList, PersonDTO.class);

        return personDTOList;
    }



    private PersonDTO mapToDTO(Person person) {
        PersonDTO personDto = modelMapper.map(person, PersonDTO.class);
        return personDto;
    }

    private <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source
                .stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }


    private long getBarCodeNumber(int cardId) {
        int id = defineCategory(cardId);
        Optional<BarCodeNumber> list = barCodeNumberRepository.findByCardId(id);
        long barCodeNumber = -1;
        if (list.isPresent()) {
            barCodeNumber = list.get().getBarCodeNumber();
            log.info("before barCode with id = {}: {}", id, barCodeNumber);
            barCodeNumberRepository.update(barCodeNumber + 1l, id);
            log.info("after barCode with id = {}: {}", id, barCodeNumberRepository.findByCardId(id).get().getBarCodeNumber());
        }

        return barCodeNumber;
    }

    private int defineCategory(int id) {
        if (id < 5) {
            return id;
        } else
            return 5;
    }
}
