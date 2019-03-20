import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Gui implements ActionListener {
    JFrame ramka = new JFrame("Graficzny interface dla Bmi");
    JPanel panel = new JPanel();
    JFXPanel wykres = new JFXPanel();
    JButton button = new JButton("Oblicz Bmi");
    JButton showChart = new JButton("Show Chart");
    Color bg = new Color(230, 230, 230);
    JTextField wzrost = new JTextField();
    JTextField wiek = new JTextField();
    JTextField waga = new JTextField();
    JTextArea wynik = new JTextArea();
    JTextArea opis = new JTextArea();
    String userGender;
    String userName;
    Dimension dimension;
    float bmi;

    public Gui() {
        bmiPane();
    }

    public Node getNode(String tagName, NodeList nodes) {
        for (int x = 0; x < nodes.getLength(); x++) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                return node;
            }
        }

        return null;
    }

    public String getNodeValue(String tagName, NodeList nodes) {
        for (int x = 0; x < nodes.getLength(); x++) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                NodeList childNodes = node.getChildNodes();
                for (int y = 0; y < childNodes.getLength(); y++) {
                    Node data = childNodes.item(y);
                    if (data.getNodeType() == Node.TEXT_NODE)
                        return data.getNodeValue();
                }
            }
        }
        return "";
    }

    public void bmiPane() {
        File file = new File("./bmi.xml");
        if (!file.exists()) {
            Object[] wybor = {"Mężczyzna", "Kobieta"};
            int i = JOptionPane.showOptionDialog(ramka, "Jakiej jesteś Płci?", "Pytanie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, wybor, wybor[1]);
            if (i == 0) {
                userGender = "Male";
            } else {
                userGender = "Female";
            }
            wybor = null;
            userName = (String) JOptionPane.showInputDialog(ramka, "Jak się nazywasz?", "pytanie", JOptionPane.PLAIN_MESSAGE, null, wybor, null);
        } else {
            getPerson();
            opis.setText("Witaj ponownie " + userName);
        }
        button.addActionListener(this);
        dimension = Toolkit.getDefaultToolkit().getScreenSize();
        ramka.setLayout(new GridLayout(2, 1));
        panel.setLayout(new GridLayout(5, 2));
        ramka.setVisible(true);
        ramka.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JLabel wzrostText = new JLabel("Podaj swój wzrost w [m]: ");
        wzrostText.setFont(new Font("New Times Roma", Font.PLAIN, 20));
        JLabel wagaText = new JLabel("Podaj swoją wagę w [kg]: ");
        wagaText.setFont(new Font("New Times Roma", Font.PLAIN, 20));
        JLabel wiekText = new JLabel("Ile masz lat?: ");
        wiekText.setFont(new Font("New Times Roma", Font.PLAIN, 20));
        panel.add(wzrostText);
        panel.add(wzrost);
        wzrost.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        panel.add(wagaText);
        panel.add(waga);
        panel.add(wiekText);
        panel.add(wiek);
        wiek.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        waga.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        showChart.addActionListener(this);
        panel.add(showChart);
        showChart.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        panel.add(button);
        button.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        panel.add(wynik);
        wynik.setBackground(bg);
        wynik.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        wynik.setEditable(false);
        ramka.add(panel);
        ramka.add(opis);
        opis.setBackground(bg);
        opis.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        opis.setLineWrap(true);
        opis.setEditable(false);
        ramka.setSize(800, 600);
        panel.setVisible(true);
        ramka.setLocation(((int) dimension.getWidth() / 2) - ramka.getWidth() / 2, (int) dimension.getHeight() / 2 - ramka.getHeight() / 2);

    }

    public void wykresy() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Stage mainStage = new Stage();
                mainStage.setTitle("Line Chart Sample");
                final NumberAxis x = new NumberAxis();
                x.setLabel("Dni");
                final NumberAxis y = new NumberAxis();
                y.setLabel("Bmi");
                LineChart<Number, Number> lineChart = new LineChart<Number, Number>(x, y);
                XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
                try {
                    File file = new File("./bmi.xml");
                    int i;
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(file);
                    NodeList root = doc.getChildNodes();

                    Node statistics = getNode("Statistics", root);
                    Node days = getNode("Days", statistics.getChildNodes());
                    NodeList nodeList = days.getChildNodes();
                    for (i = 0; i < nodeList.getLength(); i++) {
                        Node list = getNode("Day" + i, days.getChildNodes());
                        NodeList list1 = list.getChildNodes();
                        series.getData().add(new XYChart.Data<Number, Number>(i, Float.parseFloat(getNodeValue("Bmi", list1))));
                    }
                    opis.setText("A oto Twoje postępy z ostatnich " + i + " dni");
                } catch (ParserConfigurationException | SAXException | IOException pce) {
                    pce.printStackTrace();
                }
                lineChart.getData().add(series);
                Scene scene = new Scene(lineChart, 500, 400);
                wykres.setScene(scene);
            }
        });
    }

    public void plik() {
        try {
            String test = Float.toString(bmi);
            File file = new File("./bmi.xml");
            if (!file.exists()) {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();

                Element rootElement = doc.createElement("Statistics");
                doc.appendChild(rootElement);

                Element name = doc.createElement("Name");
                name.appendChild(doc.createTextNode(userName));
                rootElement.appendChild(name);

                Element gender = doc.createElement("Gender");
                gender.appendChild(doc.createTextNode(userGender));
                rootElement.appendChild(gender);

                Element days = doc.createElement("Days");
                rootElement.appendChild(days);

                Element day = doc.createElement("Day0");
                days.appendChild(day);

                Element date = doc.createElement("Date");
                date.appendChild(doc.createTextNode("19.02.1999"));
                day.appendChild(date);

                Element bmi = doc.createElement("Bmi");
                bmi.appendChild(doc.createTextNode(test));
                day.appendChild(bmi);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File("./bmi.xml"));

                transformer.transform(source, result);
            } else if (file.exists()) {
                int i;
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(file);
                NodeList root = doc.getChildNodes();

                Node statistics = getNode("Statistics", root);
                Node days = getNode("Days", statistics.getChildNodes());
                NodeList nodeList = days.getChildNodes();
                for (i = 0; i < nodeList.getLength(); i++) {
                    Node list = getNode("Day" + i, days.getChildNodes());
                    NodeList list1 = list.getChildNodes();
                    System.out.println(getNodeValue("Bmi", list1));
                }
                Element nextDay = doc.createElement("Day" + i);
                days.appendChild(nextDay);

                Element nextDate = doc.createElement("Date");
                nextDate.appendChild(doc.createTextNode(dateFormat.format(date)));
                nextDay.appendChild(nextDate);

                Element nextBmi = doc.createElement("Bmi");
                nextBmi.appendChild(doc.createTextNode(test));
                nextDay.appendChild(nextBmi);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File("./bmi.xml"));

                transformer.transform(source, result);
            }
        } catch (ParserConfigurationException | TransformerException | SAXException | IOException pce) {
            pce.printStackTrace();
        }
    }

    public void getPerson() {
        try {
            File file = new File("./bmi.xml");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            NodeList root = doc.getChildNodes();

            Node statistics = getNode("Statistics", root);
            NodeList nodeList = statistics.getChildNodes();
            userName = getNodeValue("Name", nodeList);
            userGender = getNodeValue("Gender", nodeList);
            System.out.println(userGender);
        } catch (ParserConfigurationException | SAXException | IOException pse) {
            pse.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = e.getActionCommand();
        switch (name) {
            case "Oblicz Bmi":
                try {
                    float wzrostTest = Float.parseFloat(wzrost.getText());
                    int wagaTest = Integer.parseInt(waga.getText());
                    int wiekTest = Integer.parseInt(wiek.getText());
                } catch (NumberFormatException ec) {
                    opis.setText("Jedno z pól zostało uzupełnione w niepoprawnym formacie, proszę zapisać wzrost w podanym formacie np.: 1.67 oraz wagę i wiek liczbami całkowitymi bez żadnych liter");
                    return;
                }
                if (wzrost.getText().isEmpty() | waga.getText().isEmpty() | wiek.getText().isEmpty()) {
                    opis.setText("Proszę uzupełnić puste pola");
                    return;
                }
                if (Float.parseFloat(wzrost.getText()) > 2.80 | Float.parseFloat(wzrost.getText()) < 0.50) {
                    opis.setText("Chyba został podany niepoprawyny wzrost...");
                    return;
                }
                if (Integer.parseInt(waga.getText()) > 400 | Integer.parseInt(waga.getText()) < 20) {
                    opis.setText("Chyba została podana nieprawidłowa waga...");
                    return;
                }
                bmi = Integer.parseInt(waga.getText()) / (Float.parseFloat(wzrost.getText()) * Float.parseFloat(wzrost.getText()));
                wynik.setText("Twoje Bmi to: " + bmi);
                int lata = Integer.parseInt(wiek.getText());
                if (userGender.equals("Female")) {
                    if (lata > 15) {
                        Object[] wybor = {"Nie", "Tak"};
                        int i = JOptionPane.showOptionDialog(ramka, "Czy jest Pani obecnie w ciąży?", "Pytanie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, wybor, wybor[1]);
                        System.out.println(i);
                        if (i == 1) {
                            wynik.setText("Brak");
                            opis.setText("Indeks Bmi nie będzie wyliczony poprawnie dla kobiety w ciąży");
                            return;
                        }
                    }
                    if (bmi <= 18.4) {
                        opis.setText("Niedowaga. Powinna Pani jeść więcej");
                    } else if (bmi > 18.4 & bmi <= 24.9) {
                        opis.setText("Bmi w normie. Aby zachować obecny stan rzeczy należy odżywiać się zdrowo oraz wykonywać przynajmniej 30 min ćwiczeń");
                    } else if (bmi > 24.9 & bmi <= 29.9) {
                        opis.setText("Nadwaga. Należało by ograniczyć spożywanie węglowodanów oraz cukrów oraz zwiększyć ilość aktywności fizycznej. W razie dalszych problemów zalecana jest konsultacja z lekarzem dietetykiem");
                    } else if (bmi > 29.9 & bmi <= 34.9) {
                        opis.setText("Otyłość. Zalecane jest zrezygnować ze spożywania węglowodanów oraz wszelkich cukrów. Wizyta u dietetyka jest tutaj wysoce zalecana");
                    } else if (bmi > 34.9 & bmi <= 39.9) {
                        opis.setText("Otyłość 2 stopnia. Obecna waga zagraża Pani życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Pania dalsze kroki w celu uzyskania zdrowej wagi");
                    } else if (bmi > 39.9) {
                        opis.setText("Otyłość olbrzymia. Obecna waga zagraża Pani życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Pania dalsze kroki w celu uzyskania zdrowej wagi");
                    }
                } else if (userGender.equals("Male")) {
                    opis.setText("Pana Bmi to: " + bmi);
                    if (bmi <= 18.4) {
                        opis.setText("Niedowaga. Powinien Pan jeść więcej");
                    } else if (bmi > 18.4 & bmi <= 24.9) {
                        opis.setText("Bmi w normie. Aby zachować obecny stan rzeczy należy odżywiać się zdrowo oraz wykonywać przynajmniej 30 min ćwiczeń");
                    } else if (bmi > 24.9 & bmi <= 29.9) {
                        opis.setText("Nadwaga. Należało by ograniczyć spożywanie węglowodanów oraz cukrów oraz zwiększyć ilość aktywności fizycznej. W razie dalszych problemów zalecana jest konsultacja z lekarzem dietetykiem");
                    } else if (bmi > 29.9 & bmi <= 34.9) {
                        opis.setText("Otyłość. Zalecane jest zrezygnować ze spożywania węglowodanów oraz wszelkich cukrów. Wizyta u dietetyka jest tutaj wysoce zalecana");
                    } else if (bmi > 34.9 & bmi <= 39.9) {
                        opis.setText("Otyłość 2 stopnia. Obecna waga zagraża Pana życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Panem dalsze kroki w celu uzyskania zdrowej wagi");
                    } else if (bmi > 39.9) {
                        opis.setText("Otyłość olbrzymia. Obecna waga zagraża Pana życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Panem dalsze kroki w celu uzyskania zdrowej wagi");
                    }
                }
                if (lata > 59 & lata < 120) {
                    if (userGender.equals("Female")) {
                        opis.setText("Wskaźnik Bmi dla osoby w Pani wieku nie może zostać poprawnie wyliczone. Wraz z wiekiem maleje masa mięśni i kości przez co indeks Bmi może zostać obliczony niepoprawnie");

                    } else if (userGender.equals("Male")) {
                        opis.setText("Wskaźnik Bmi dla osoby w Pana wieku nie może zostać poprawnie wyliczone. Wraz z wiekiem maleje masa mięśni i kości przez co indeks Bmi może zostać obliczony niepoprawnie");
                    }
                } else if (lata < 18) {
                    opis.setText("Indeks Bmi wyliczany jest poprawnie dla osób dorosłych. Dla dzieci i młodzieży mimo tego że wyliczenia są podobne to kryteria są ustawiane inaczej z kilku powodów");
                } else if (lata >= 120) {
                    opis.setText("Chyba podany został trochę zawyżony wiek...");
                }
                plik();
                break;
            case "Show Chart":
                JFrame chart = new JFrame("Chart");
                chart.setSize(ramka.getWidth(),ramka.getHeight());
                chart.setLocation(((int) dimension.getWidth()/2) + ramka.getWidth()/2, (int) dimension.getHeight() / 2 - ramka.getHeight() / 2);
                chart.add(wykres);
                wykresy();
                wykres.setVisible(true);
                chart.setVisible(true);
                break;
        }
    }
}
