import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
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
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Gui implements ActionListener,Runnable {
    JFrame frame = new JFrame("Graficzny interface dla Bmi");
    JPanel panel = new JPanel();
    JFXPanel chart = new JFXPanel();
    JButton calcBmi = new JButton("Oblicz Bmi");
    JButton showChart = new JButton("Pokaż Wykres");
    JButton showLogs = new JButton("Pokaż moje Dane");
    JButton showCanvas = new JButton("Pokaż kanwy");
    Color bg = new Color(230, 230, 230);
    List<String> dates = new ArrayList<>();
    List<Number> bmis = new ArrayList<>();
    JTextField height = new JTextField();
    JTextField age = new JTextField();
    JTextField weight = new JTextField();
    JTextArea result = new JTextArea();
    JTextArea description = new JTextArea();
    String userGender;
    String userName;
    Dimension dimension;
    float bmi;
    public Gui() {
        bmiPane();
    }


    public Node getNode(String tagName, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                return node;
            }
        }
        return null;
    }

    public String getNodeValue(String tagName, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node data = childNodes.item(j);
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
            int i = JOptionPane.showOptionDialog(frame, "Jakiej jesteś Płci?", "Pytanie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, wybor, wybor[1]);
            if (i == 0) {
                userGender = "Male";
            } else if (i == 1) {
                userGender = "Female";
            } else {
                System.exit(0);
            }
            wybor = null;
            userName = (String) JOptionPane.showInputDialog(frame, "Jak się nazywasz?", "pytanie", JOptionPane.PLAIN_MESSAGE, null, wybor, null);
            if (userName == null) {
                System.exit(0);
            }
        } else {
            getPerson();
            description.setText("Witaj ponownie " + userName);
        }
        calcBmi.addActionListener(this);
        dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLayout(new GridLayout(2, 1));
        panel.setLayout(new GridLayout(5, 2));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel heightText = new JLabel("Podaj swój wzrost w [m]: ");
        heightText.setFont(new Font("New Times Roma", Font.PLAIN, 20));
        JLabel weightText = new JLabel("Podaj swoją wagę w [kg]: ");
        weightText.setFont(new Font("New Times Roma", Font.PLAIN, 20));
        JLabel ageText = new JLabel("Ile masz lat?: ");
        ageText.setFont(new Font("New Times Roma", Font.PLAIN, 20));
        panel.add(heightText);
        panel.add(height);
        height.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        panel.add(weightText);
        panel.add(weight);
        panel.add(ageText);
        panel.add(age);
        age.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        weight.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        showChart.addActionListener(this);
        panel.add(showChart);
        showChart.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        panel.add(calcBmi);
        calcBmi.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        showLogs.addActionListener(this);
        showLogs.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        panel.add(showLogs);
        showCanvas.addActionListener(this);
        panel.add(showCanvas);
        //panel.add(result);
        result.setBackground(bg);
        result.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        result.setEditable(false);
        frame.add(panel);
        JScrollPane scrollPane = new JScrollPane(description);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane);
        description.setBackground(bg);
        description.setFont(new Font("New Times Roma", Font.PLAIN, 26));
        description.setLineWrap(true);
        description.setEditable(false);
        frame.setSize(700, 600);
        panel.setVisible(true);
        frame.setLocation(((int) dimension.getWidth() / 2) - frame.getWidth() / 2, (int) dimension.getHeight() / 2 - frame.getHeight() / 2);

    }
    public void charts() {
        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                NumberAxis x = new NumberAxis();
                x.setLabel("Pomiary");
                NumberAxis y = new NumberAxis();
                y.setLabel("Bmi");
                LineChart<Number, Number> lineChart = new LineChart<Number, Number>(x, y);
                XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
                Scene scene = new Scene(new Group());
                /*try-catch został użyty ponieważ parser xml rzuca wyjątek mówiący o problemie z jego konfiguracją i wyjątek rzucany gdy napotka problemy z rozparsowaniem pliku
                klasa File rzucają wyjątkek Wejścia/Wyjścia*/
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
                        series.getData().add(new XYChart.Data<Number, Number>(i+1, Float.parseFloat(getNodeValue("Bmi", list1))));
                    }
                    description.setText("A oto Twoje postępy z ostatnich " + i + " pomiarów");
                } catch (ParserConfigurationException | SAXException | IOException pce) {
                    pce.printStackTrace();
                }
                lineChart.getData().add(series);
                ((Group) scene.getRoot()).getChildren().add(lineChart);
                chart.setScene(scene);
            }
        });
    }
    public void getData(){
        /*try-catch został użyty ponieważ parser xml rzuca wyjątek mówiący o problemie z jego konfiguracją i wyjątek rzucany gdy napotka problemy z rozparsowaniem pliku
        klasa File rzucają wyjątkek Wejścia/Wyjścia*/
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
                dates.add(getNodeValue("Date",list1));
                bmis.add(Float.parseFloat(getNodeValue("Bmi",list1)));
            }
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            pce.printStackTrace();
        }
    }
    public void getPerson() {
        /*try-catch został użyty ponieważ parser xml rzuca wyjątek mówiący o problemie z jego konfiguracją i wyjątek rzucany gdy napotka problemy z rozparsowaniem pliku
        klasa File rzucają wyjątkek Wejścia/Wyjścia*/
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
        } catch (ParserConfigurationException | SAXException | IOException pse) {
            pse.printStackTrace();
        }
    }
    public void files() {
        /*try-catch został użyty ponieważ parser xml rzuca wyjątek mówiący o problemie z jego konfiguracją i wyjątek rzucany gdy napotka problemy z rozparsowaniem pliku
        klasa File rzucają wyjątkek Wejścia/Wyjścia*/
        try {
            String test = Float.toString(bmi);
            File file = new File("./bmi.xml");
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = new Date();
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
                date.appendChild(doc.createTextNode(dateFormat.format(currentDate)));
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
                nextDate.appendChild(doc.createTextNode(dateFormat.format(currentDate)));
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

    @Override
    public void run() {
        rysuj();
    }
    public void rysuj(){
        JFrame ramka = new JFrame();
        ramka.setSize(400,600);
        ramka.setVisible(true);
        Canvas canvas = new Canvas(){
            public void paint(Graphics g) {
                try {
                    for (int i = 0; i < 40; i++) {
                        Thread.sleep(100);
                        //g.drawOval(40,40,i,i);
                        g.fillOval(40, 40, i, i);
                    }
                /*g.setColor(Color.red);
                g.drawOval(40,40,40,40);
                g.fillOval(40,40,40,40);
                g.drawOval(100,40,40,40);
                g.fillOval(100,40,40,40);
                g.drawOval(60,120,100,40);*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("hehe");
            }
        };
        canvas.setSize(200,300);
        ramka.add(canvas);
        ramka.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = e.getActionCommand();
        switch (name) {
            case "Oblicz Bmi":
                //try-catch użyty został ponieważ chcę zabezpieczyć aplikację przed wprowadzeniem źle sformatowanych wartości np: gdyby ktoś wprowadził zamiast 1.80, E32,0
                try {
                    float heightTest = Float.parseFloat(height.getText());
                    int weightTest = Integer.parseInt(weight.getText());
                    int ageTest = Integer.parseInt(age.getText());
                } catch (NumberFormatException ec) {
                    description.setText("Jedno z pól zostało uzupełnione w niepoprawnym formacie, proszę zapisać wzrost w podanym formacie np.: 1.67 oraz wagę i wiek liczbami całkowitymi bez żadnych liter");
                    return;
                }
                if (height.getText().isEmpty() | weight.getText().isEmpty() | age.getText().isEmpty()) {
                    description.setText("Proszę uzupełnić puste pola");
                    return;
                }
                if (Float.parseFloat(height.getText()) > 2.80 | Float.parseFloat(height.getText()) < 0.50) {
                    description.setText("Chyba został podany niepoprawyny wzrost...");
                    return;
                }
                if (Integer.parseInt(weight.getText()) > 400 | Integer.parseInt(weight.getText()) < 20) {
                    description.setText("Chyba została podana nieprawidłowa waga...");
                    return;
                }
                bmi = Integer.parseInt(weight.getText()) / (Float.parseFloat(height.getText()) * Float.parseFloat(height.getText()));
                result.setText("Twoje Bmi to: " + bmi);
                int lata = Integer.parseInt(age.getText());
                if (userGender.equals("Female")) {
                    if (lata > 15) {
                        Object[] wybor = {"Nie", "Tak"};
                        int i = JOptionPane.showOptionDialog(frame, "Czy jest Pani obecnie w ciąży?", "Pytanie", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, wybor, wybor[1]);
                        System.out.println(i);
                        if (i == 1) {
                            result.setText("Brak");
                            description.setText("Indeks Bmi nie będzie wyliczony poprawnie dla kobiety w ciąży");
                            return;
                        }
                    }
                    if (bmi <= 18.4) {
                        description.setText("Niedowaga. Powinna Pani jeść więcej");
                    } else if (bmi > 18.4 & bmi <= 24.9) {
                        description.setText("Bmi w normie. Aby zachować obecny stan rzeczy należy odżywiać się zdrowo oraz wykonywać przynajmniej 30 min ćwiczeń");
                    } else if (bmi > 24.9 & bmi <= 29.9) {
                        description.setText("Nadwaga. Należało by ograniczyć spożywanie węglowodanów oraz cukrów oraz zwiększyć ilość aktywności fizycznej. W razie dalszych problemów zalecana jest konsultacja z lekarzem dietetykiem");
                    } else if (bmi > 29.9 & bmi <= 34.9) {
                        description.setText("Otyłość. Zalecane jest zrezygnować ze spożywania węglowodanów oraz wszelkich cukrów. Wizyta u dietetyka jest tutaj wysoce zalecana");
                    } else if (bmi > 34.9 & bmi <= 39.9) {
                        description.setText("Otyłość 2 stopnia. Obecna waga zagraża Pani życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Pania dalsze kroki w celu uzyskania zdrowej wagi");
                    } else if (bmi > 39.9) {
                        description.setText("Otyłość olbrzymia. Obecna waga zagraża Pani życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Pania dalsze kroki w celu uzyskania zdrowej wagi");
                    }
                } else if (userGender.equals("Male")) {
                    description.setText("Pana Bmi to: " + bmi);
                    if (bmi <= 18.4) {
                        description.setText("Niedowaga. Powinien Pan jeść więcej");
                    } else if (bmi > 18.4 & bmi <= 24.9) {
                        description.setText("Bmi w normie. Aby zachować obecny stan rzeczy należy odżywiać się zdrowo oraz wykonywać przynajmniej 30 min ćwiczeń");
                    } else if (bmi > 24.9 & bmi <= 29.9) {
                        description.setText("Nadwaga. Należało by ograniczyć spożywanie węglowodanów oraz cukrów oraz zwiększyć ilość aktywności fizycznej. W razie dalszych problemów zalecana jest konsultacja z lekarzem dietetykiem");
                    } else if (bmi > 29.9 & bmi <= 34.9) {
                        description.setText("Otyłość. Zalecane jest zrezygnować ze spożywania węglowodanów oraz wszelkich cukrów. Wizyta u dietetyka jest tutaj wysoce zalecana");
                    } else if (bmi > 34.9 & bmi <= 39.9) {
                        description.setText("Otyłość 2 stopnia. Obecna waga zagraża Pana życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Panem dalsze kroki w celu uzyskania zdrowej wagi");
                    } else if (bmi > 39.9) {
                        description.setText("Otyłość olbrzymia. Obecna waga zagraża Pana życiu lub zdrowiu, należy odwiedzić dietetyka który omówi z Panem dalsze kroki w celu uzyskania zdrowej wagi");
                    }
                }
                if (lata > 59 & lata < 120) {
                    if (userGender.equals("Female")) {
                        description.setText("Wskaźnik Bmi dla osoby w Pani wieku nie może zostać poprawnie wyliczone. Wraz z wiekiem maleje masa mięśni i kości przez co indeks Bmi może zostać obliczony niepoprawnie");

                    } else if (userGender.equals("Male")) {
                        description.setText("Wskaźnik Bmi dla osoby w Pana wieku nie może zostać poprawnie wyliczone. Wraz z wiekiem maleje masa mięśni i kości przez co indeks Bmi może zostać obliczony niepoprawnie");
                    }
                } else if (lata < 18) {
                    description.setText("Indeks Bmi wyliczany jest poprawnie dla osób dorosłych. Dla dzieci i młodzieży mimo tego że wyliczenia są podobne to kryteria są ustawiane inaczej z kilku powodów");
                } else if (lata >= 120) {
                    description.setText("Chyba podany został trochę zawyżony wiek...");
                }
                files();
                break;
            case "Pokaż Wykres":
                JFrame chartFrame = new JFrame("Wykres");
                chartFrame.setLayout(new GridLayout(2,1));
                JPanel first = new JPanel();
                JPanel second = new JPanel();
                chartFrame.setSize(frame.getWidth()-(frame.getWidth()/4),frame.getHeight()-(frame.getHeight()/4));
                chartFrame.setLocation(((int) dimension.getWidth()/2) + frame.getWidth()/2, (int) dimension.getHeight() / 2 - frame.getHeight() / 2);
                chartFrame.add(chart);
                chartFrame.add(first);
                chartFrame.add(second);
                charts();
                chart.setVisible(true);
                chartFrame.setVisible(true);
                break;
            case "Pokaż moje Dane":
                description.setText("A oto twoje dane do tej pory: ");
                description.setText(description.getText()+" \n");
                getData();
                for(int i = 0;i<dates.size();i++){
                    description.setText(description.getText()+" "+dates.get(i)+": "+bmis.get(i)+"\n");
                }
                break;
            case "Pokaż kanwy":
                Thread test = new Thread(this);
                test.start();
                break;
        }
    }
}
