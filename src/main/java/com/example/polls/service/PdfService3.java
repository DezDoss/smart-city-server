package com.example.polls.service;

import com.example.polls.model.Person;
import com.example.polls.repository.PersonRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfService3 {
    private static final int COORDINATE_X = 193;
    private static final int COORDINATE_Y = 706;

    private PersonRepository personRepository;

    @Autowired
    public void setPersonRepository(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Long> getZippedFile() throws IOException, DocumentException {
        List<Person> personList = personRepository.findAllByStatus("Отправлен в обработку");
        List<Person> firstTenPersonList = personList.subList(0, 10);
        ArrayList<String> invalidList = createFile(firstTenPersonList);
        String sourceFile = "result";
        FileOutputStream fos = new FileOutputStream("zip/edited.zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);


        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();

        for(String str : invalidList) {
            File file = new File(str);
            file.deleteOnExit();
        }
            List<Long> idList = new ArrayList<>();
        for(Person person : firstTenPersonList) {
            idList.add(person.getId());
        }

        return idList;
    }

    private ArrayList<String> createFile(List<Person> personList) throws IOException, DocumentException {
        String resultPathFront = "result/front.pdf";
        String resultPathBack = "result/back.pdf";
        String path = "blank/setka.pdf";
        PdfReader pdfReader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(pdfReader, new FileOutputStream(resultPathFront));
        PdfReader pdfReaderBack = new PdfReader(path);
        PdfStamper back = new PdfStamper(pdfReaderBack, new FileOutputStream(resultPathBack));

        PdfContentByte backContent = back.getOverContent(1);
        PdfContentByte content = stamper.getOverContent(1);
        int coordinateY = COORDINATE_Y;
        int moveY = 163;
        int column = 1;


        for (int i = 0; i < personList.size(); i++) {
            ArrayList<String> pathList = definePdfCategory(personList.get(i).getCardType());
            String blankFrontPath = pathList.get(0);
            String blankBackPath = pathList.get(1);
            String barCode = personList.get(i).getBarCode();
            String editedBarCode = barCode.substring(0, 3) + " " + barCode.substring(3,6) + " " + barCode.substring(6);
            String iin = personList.get(i).getIin();
            int fullNameLength = personList.get(i).getFirstName().length() + personList.get(i).getSecondName().length();
            String fullName = fullNameLength > 17 ?
                    personList.get(i).getSecondName() + " " + personList.get(i).getFirstName().charAt(0) + "." :
                    personList.get(i).getSecondName() + " " + personList.get(i).getFirstName();
            String editedIin = iin.substring(0, 4) + " " + iin.substring(4,8) + " " + iin.substring(8);
            if (column == 1) {
                setImage(blankFrontPath, COORDINATE_X - 159, coordinateY - 40, content);
                setImage(blankBackPath, COORDINATE_X* 2 + 86 - 160, coordinateY - 40, backContent);
                setPhoto(personList.get(i).getImage(), content, COORDINATE_X, coordinateY);
                setText(fullName, COORDINATE_X - 80, coordinateY - 18, content, 10);
                setText(editedIin,COORDINATE_X + 33, coordinateY - 18, content, 10);

                setBarCode(barCode, COORDINATE_X*2 + 86 + 4, coordinateY, backContent);
                setText(editedBarCode, COORDINATE_X * 2 - 4, coordinateY + 84, backContent, 16);
                column++;

            } else {
                setImage(blankFrontPath, COORDINATE_X* 2 + 86 - 160, coordinateY - 40, content);
                setImage(blankBackPath, COORDINATE_X - 159, coordinateY - 40, backContent);
                setPhoto(personList.get(i).getImage(), content,COORDINATE_X * 2 + 86, coordinateY);
                setText(fullName, COORDINATE_X * 2 + 6, coordinateY - 18, content, 10);
                setText(editedIin, COORDINATE_X*2 + 119, coordinateY - 18, content, 10);

                setBarCode(barCode, COORDINATE_X + 9, coordinateY, backContent);
                setText(editedBarCode, COORDINATE_X - 91, coordinateY + 84, backContent, 16);

                column = 1;
                coordinateY -= moveY;
            }
        }


        back.close();
        stamper.close();

        ArrayList<String> listPath = new ArrayList<>();
        listPath.add(resultPathFront);
        listPath.add(resultPathBack);
        return  listPath;
    }

    private void setText(String text, int x, int y, PdfContentByte content, int fontSize) throws IOException, DocumentException {
        BaseFont bfComic = BaseFont.createFont("Roboto-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        content.setFontAndSize(bfComic, fontSize);
        content.beginText();
        content.showTextAligned(Element.ALIGN_CENTER, text, x, y, 0f);
        content.endText();
        content.stroke();
    }

    private void setBarCode(String barCode, int x, int y, PdfContentByte content) {
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

    private void setPhoto(byte[] bytes, PdfContentByte content, int x, int y) throws IOException, DocumentException {
        Image image = Image.getInstance(bytes);
        image.scaleAbsoluteHeight(100);
        image.scaleAbsoluteWidth(75);
        image.setAbsolutePosition(x, y);
        content.addImage(image);

    }

    private void setImage(String path, int x, int y, PdfContentByte content) throws IOException, DocumentException {
        Image image = Image.getInstance(path);
        image.scaleAbsoluteHeight(162);
        image.scaleAbsoluteWidth(254);
        image.setAbsolutePosition(x, y);
        content.addImage(image);

    }



    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
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

    private ArrayList<String> definePdfCategory(String category) {
        String pdfFrontPath = "blank/invalid-front.png";
        String pdfBackPath = "blank/invalid-back.png";
        if(category.equals("Пенсионер")){
            pdfFrontPath = "blank/pensioner-front.png";
            pdfBackPath = "blank/pensioner-back.png";
        } else if(category.equals("Школьник")) {
            pdfFrontPath = "blank/school-front.png";
            pdfBackPath = "blank/school-back.png";
        } else if(category.equals("Студент")) {
            pdfFrontPath = "blank/student-front.png";
            pdfBackPath = "blank/student-back.png";
        }
        ArrayList<String> pathList = new ArrayList<>();
        pathList.add(pdfFrontPath);
        pathList.add(pdfBackPath);

        return pathList;
    }
}
