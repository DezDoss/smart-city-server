package com.example.polls.service;


import com.example.polls.model.Person;
import com.example.polls.repository.PersonRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PdfService2 {
    private static final int COORDINATE_Y = 706;

    private PersonRepository personRepository;

    @Autowired
    public void setPersonRepository(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

   public void generatePdf(List<Long> idList) throws IOException, DocumentException {
       File file = new File("result/example.pdf");
       Document document = new Document(PageSize.A4);
       PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
       writer.setPageEmpty(false);
       document.open();

       int coordinateY = 660;
       int j = 0;
       for(int i = 0; i < idList.size(); i++) {
           Optional<Person> person = personRepository.findById(idList.get(i));
           if(person.isPresent()){
               String path = defineCategory(person.get().getCardId());
               Image image = Image.getInstance(path);
               image.scaleAbsolute(595, 161.5f);
               image.setAbsolutePosition(0, coordinateY - 161.5f*j);
               document.add(image);
               if((j + 1) % 5 == 0) {
                   j = 0;
                   document.newPage();
               } else {
                   j++;
               }
           }
       }

       document.close();
       setImage(idList);
   }

    public  void setImage(List<Long> idList) throws IOException, DocumentException {
        String path = "result/example.pdf";
        PdfReader pdfReader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(pdfReader, new FileOutputStream("result/result.pdf"));


        int coordinateY = COORDINATE_Y;
        int j = 0;
        int page = 1;
        for(int i = 0; i < idList.size(); i++) {
            PdfContentByte content = stamper.getOverContent(page);
            Optional<Person> person = personRepository.findById(idList.get(i));
            if(person.isPresent()) {
                addImage(content, 193, coordinateY - 161.9f * j, person.get().getImage());
                setText(person.get().getSecondName() + " " + person.get().getFirstName(), 105, coordinateY - 22 - 161.9f * j, content);
                setText(person.get().getIin(), 230, coordinateY - 22 - 161.9f * j, content);
                setText(person.get().getBarCode(), 350, coordinateY + 80 - 161.9f * j, content);
                setBarCode(person.get().getBarCode(), 500, coordinateY + 74 - 161.9f * j, content);

                if((j + 1) % 5 == 0) {
                    j = 0;
                    page++;
                } else {
                    j++;
                }
            }
        }
        stamper.close();
    }

    public static void addImage(PdfContentByte content, float x, float y, byte[] imageByte) throws IOException, DocumentException {
        Image image = Image.getInstance(imageByte);
        image.scaleAbsolute(77, 100);
        image.setAbsolutePosition(x, y);
        content.addImage(image);
    }

    private static void setText(String text, float x, float y, PdfContentByte content) throws IOException, DocumentException {
        BaseFont bfComic = BaseFont.createFont("Calibri.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        content.setFontAndSize(bfComic, 10);
        content.beginText();
        content.showTextAligned(Element.ALIGN_CENTER, text, x, y, 0f);
        content.endText();
        content.stroke();
    }

    private static void setBarCode(String barCode, float x, float y, PdfContentByte content) {
        Barcode128 code128 = new Barcode128();
        code128.setCode(barCode);
        code128.setCodeType(Barcode128.CODE128);
        code128.setSize(5);
        code128.setBarHeight(20);
        code128.setX(0.5f);
        code128.setFont(null);
        PdfTemplate template = code128.createTemplateWithBarcode(
                content, BaseColor.BLACK, BaseColor.BLACK);
        content.addTemplate(template, x, y);
    }

    public String defineCategory(long cardId) {
        String category = "";
        switch ((int) cardId) {
            case 1:
                category = "blank/schoolboy.jpg";
                break;
            case 2:
                category = "blank/invalid1.jpg";
                break;
            case 3:
                category = "blank/student.jpg";
                break;
            case 4:
                category = "blank/pensioner.jpg";
                break;
        }

        return category;
    }
}
