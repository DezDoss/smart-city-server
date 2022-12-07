package com.example.polls.service;

import com.example.polls.model.Person;
import com.example.polls.repository.PersonRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfService {
    private static final int COORDINATE_X = 193;
    private static final int COORDINATE_Y = 706;

    private PersonRepository personRepository;

    @Autowired
    public void setPersonRepository(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Long> getZippedFile() throws IOException, DocumentException {
        List<Person> personList = personRepository.findAllByStatus("Отправлен в обработку");
//        System.out.println(personList.size());
//        int otherLength = 0;
//        int pensionerLength = defineLength(personList, "Пенсионер");
//        int pupilLength = defineLength(personList, "Школьник");
//        int studentLength = defineLength(personList, "Студент");
//        int mnogodetLength = defineLength(personList, "Многодетные");
//
//        otherLength += defineLength(personList, "Инвалид 1 гр");
//        otherLength += defineLength(personList, "Инвалид 2 гр");
//        otherLength += defineLength(personList, "Инвалид 3 гр");
//        otherLength += defineLength(personList, "Опекун");
//        otherLength += defineLength(personList, "Ветеран");
//
//        System.out.println(pensionerLength);
//        System.out.println(pupilLength);
//        System.out.println(studentLength);
//        System.out.println(mnogodetLength);
//        System.out.println(otherLength);
        ArrayList<Person> pensioners = new ArrayList<>();
        for(Person person : personList) {
            if(person.getCardType().equals("Пенсионер")) {
                pensioners.add(person);
            }
            if(pensioners.size() == 10) {
                break;
            }
        }
//
        ArrayList<Person> pupils = new ArrayList<>();
        for(Person person : personList) {
            if(person.getCardType().equals("Школьник")) {
                pupils.add(person);
            }
            if(pupils.size() == 10) {
                break;
            }
        }
//
        ArrayList<Person> students = new ArrayList<>();
        for(Person person : personList) {
            if(person.getCardType().equals("Студент")) {
                students.add(person);
            }
            if (students.size() == 10) {
                break;
            }
        }

        ArrayList<Person> mnogodentie = new ArrayList<>();
        for(Person person : personList) {
            if(person.getCardType().equals("Многодетные")) {
                mnogodentie.add(person);
            }
            if(mnogodentie.size() == 10) {
                break;
            }
        }
        ArrayList<Person> otherList = new ArrayList<>();
        for(Person person : personList) {
            if(person.getCardType().equals("Инвалид 1 гр") || person.getCardType().equals("Инвалид 2 гр") ||
            person.getCardType().equals("Инвалид 3 гр") || person.getCardType().equals("Опекун") ||
            person.getCardType().equals("Ветеран")) {
                otherList.add(person);
            }
            if(otherList.size() == 10) {
                break;
            }
        }
        if(pensioners.size() < 10) pensioners = new ArrayList<>();
        if(pupils.size() < 10) pupils = new ArrayList<>();
        if(students.size() < 10) students = new ArrayList<>();
        if(mnogodentie.size() < 10) mnogodentie = new ArrayList<>();
        if(otherList.size() < 10) otherList = new ArrayList<>();

//
        List<Long> idList = new ArrayList<>();
        createFile("pdf/front-pensioner.pdf", "pdf/back-pensioner.pdf", pensioners, idList);
        createFile("pdf/front-schoolboy.pdf", "pdf/back-schoolboy.pdf", pupils, idList);
        createFile("pdf/front-student.pdf", "pdf/back-student.pdf", students, idList);
        createFile("pdf/front-mnogodet.pdf", "pdf/back-mnogodet.pdf", mnogodentie, idList);
        createFile("pdf/front-inv-opekun-veteran.pdf", "pdf/back-inv-opekun-veteran.pdf", otherList, idList);

        String sourceFile = "result";
        FileOutputStream fos = new FileOutputStream("zip/edited.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
        System.out.println("end");
        File file = new File("result");
        FileUtils.cleanDirectory(file);

        return idList;
    }

    private  void setText(String text, int x, int y, PdfContentByte content, int size) throws IOException, DocumentException {
        BaseFont bfComic = BaseFont.createFont("Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        content.setFontAndSize(bfComic, size);
        content.beginText();
        content.showTextAligned(Element.ALIGN_CENTER, text, x, y, 0f);
        content.endText();
        content.stroke();
    }

    private  void setBarCode(String barCode, int x, int y, PdfContentByte content) {
        Barcode128 code128 = new Barcode128();
        code128.setCode(barCode);
        code128.setCodeType(Barcode128.CODE128);
        code128.setSize(5);
        code128.setBarHeight(20);
        code128.setX(0.8f);
        code128.setFont(null);
        PdfTemplate template = code128.createTemplateWithBarcode(
                content, BaseColor.BLACK, BaseColor.BLACK);
        content.addTemplate(template, x, y + 80);
    }

    private  void setImage(byte[] img, PdfContentByte content, int x, int y) throws IOException, DocumentException {
        Image image = Image.getInstance(img);
        image.scaleAbsoluteHeight(104);
        image.scaleAbsoluteWidth(77);
        image.setAbsolutePosition(x, y - 2);
        content.addImage(image);

    }

    private  void createFile(String frontPath, String backPath, List<Person> personList, List<Long> idList) throws IOException, DocumentException {
        if(personList.size() == 0) return;
        String resultPathFront = "result/out-" + frontPath.substring(4);
        String resultPathBack = "result/out-" + backPath.substring(4);
        String path = frontPath;
        PdfReader pdfReader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(pdfReader, new FileOutputStream(resultPathFront));
        String path2 = backPath;
        PdfReader pdfReaderBack = new PdfReader(path2);
        PdfStamper back = new PdfStamper(pdfReaderBack, new FileOutputStream(resultPathBack));

        PdfContentByte backContent = back.getOverContent(1);
        PdfContentByte content = stamper.getOverContent(1);
        int coordinateY = COORDINATE_Y;
        int moveY = 163;
        int column = 1;


        for (Person person : personList) {
            idList.add(person.getId());
            String barCode = person.getBarCode();
            String editedBarCode = barCode.substring(0, 3) + " " + barCode.substring(3,6) + " " + barCode.substring(6);
            String iin = person.getIin();
            int fullNameLength = person.getFirstName().length() + person.getSecondName().length();
            String fullName = fullNameLength > 17 ?
                    person.getSecondName() + " " + person.getFirstName().charAt(0) + "." :
                    person.getSecondName() + " " + person.getFirstName();
            String editedIin = iin.substring(0, 4) + " " + iin.substring(4,8) + " " + iin.substring(8);
            if (column == 1) {
                setImage(person.getImage(), content, COORDINATE_X, coordinateY);
                setText(fullName, COORDINATE_X - 80, coordinateY - 18, content, 10);
                setText(editedIin, COORDINATE_X + 33, coordinateY - 18, content, 10);

                setBarCode(barCode, COORDINATE_X*2 + 86 + 4, coordinateY, backContent);
                setText(editedBarCode, COORDINATE_X * 2 - 4, coordinateY + 84, backContent, 16);
                column++;

            } else {
                setImage(person.getImage(), content,COORDINATE_X * 2 + 85, coordinateY - 1);
                setText(fullName, COORDINATE_X * 2 + 86 - 80, coordinateY - 18, content, 10);
                setText(editedIin, COORDINATE_X*2 + 86 + 33, coordinateY - 18, content, 10);

                setBarCode(barCode, COORDINATE_X + 9, coordinateY, backContent);
                setText(editedBarCode, COORDINATE_X - 91, coordinateY + 84, backContent, 16);

                column = 1;
                coordinateY -= moveY;
            }
        }

        back.close();
        stamper.close();

//        ArrayList<String> listPath = new ArrayList<>();
//        listPath.add(resultPathFront);
//        listPath.add(resultPathBack);
//        return  listPath;
    }

    private  void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    private int defineLength(List<Person> personList, String category) {
        int length = 0;

        for(Person person : personList) {
            if(person.getCardType().equals(category)) {
                length++;
            }
        }

        return length;
    }

    private void addToList(List<Person> personList, List<Person> categoryList, String category) {
        for(Person person : personList) {
            if(person.getCardType().equals(category)) {
                categoryList.add(person);
            }
        }
    }
}
