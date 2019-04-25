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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class Gui implements ActionListener, Runnable {

    Locale english = new Locale("en","GB");
    Locale polish = new Locale("pl","PL");
    Locale russian = new Locale("ru","RU");
    ResourceBundle text = ResourceBundle.getBundle("UI",polish);
    JFrame frame = new JFrame(text.getString("title"));
    JPanel panel = new JPanel();
    JFXPanel chart = new JFXPanel();
    JButton calcBmi = new JButton(text.getString("calculate_button"));
    JButton showChart = new JButton(text.getString("chart_button"));
    JButton showLogs = new JButton(text.getString("data_button"));
    JButton showCanvas = new JButton(text.getString("visualize_button"));
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
            Object[] wybor = {text.getString("male"), text.getString("female")};
            int i = JOptionPane.showOptionDialog(frame, text.getString("gender"), text.getString("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, wybor, wybor[1]);
            if (i == 0) {
                userGender = "Male";
            } else if (i == 1) {
                userGender = "Female";
            } else {
                System.exit(0);
            }
            wybor = null;
            userName = (String) JOptionPane.showInputDialog(frame, text.getString("name"), text.getString("question"), JOptionPane.PLAIN_MESSAGE, null, wybor, null);
            if (userName == null) {
                System.exit(0);
            }
        } else {
            getPerson();
            description.setText(text.getString("welcome")+ " " + userName);
        }
        calcBmi.addActionListener(this);
        dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLayout(new GridLayout(2, 1));
        panel.setLayout(new GridLayout(5, 2));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel heightText = new JLabel(text.getString("height"));
        heightText.setFont(new Font("Arial MS Unicode", Font.PLAIN, 20));
        JLabel weightText = new JLabel(text.getString("weight"));
        weightText.setFont(new Font("Arial MS Unicode", Font.PLAIN, 20));
        JLabel ageText = new JLabel(text.getString("age"));
        ageText.setFont(new Font("Arial MS Unicode", Font.PLAIN, 20));
        panel.add(heightText);
        panel.add(height);
        height.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        panel.add(weightText);
        panel.add(weight);
        panel.add(ageText);
        panel.add(age);
        age.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        weight.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        showChart.addActionListener(this);
        panel.add(showChart);
        showChart.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        panel.add(calcBmi);
        calcBmi.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        showLogs.addActionListener(this);
        showLogs.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        panel.add(showLogs);
        showCanvas.addActionListener(this);
        showCanvas.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        panel.add(showCanvas);
        //panel.add(result);
        result.setBackground(bg);
        result.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        result.setEditable(false);
        frame.add(panel);
        JScrollPane scrollPane = new JScrollPane(description);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane);
        description.setBackground(bg);
        description.setFont(new Font("Arial MS Unicode", Font.PLAIN, 26));
        description.setLineWrap(true);
        description.setEditable(false);
        frame.setSize(700, 600);
        panel.setVisible(true);
        frame.setLocation(((int) dimension.getWidth() / 2) - frame.getWidth() / 2, (int) dimension.getHeight() / 2 - frame.getHeight() / 2);

    }

    public void charts() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
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
                        series.getData().add(new XYChart.Data<Number, Number>(i + 1, Float.parseFloat(getNodeValue("Bmi", list1))));
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

    public void getData() {
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
                dates.add(getNodeValue("Date", list1));
                bmis.add(Float.parseFloat(getNodeValue("Bmi", list1)));
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

    public void rysuj() {
        JFrame ramka = new JFrame();
        ramka.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        ramka.setSize(400, 300);
        ramka.setLocation(((int) dimension.getWidth() / 2) + frame.getWidth() / 2, (int) dimension.getHeight() / 2 - frame.getHeight() / 2);
        ramka.setVisible(true);
        Canvas canvas = new Canvas() {
            public void paint(Graphics g) {
                //try-catch został użyty ponieważ metoda statyczna "sleep" może rzucić wyjątek InterruptedException
                try {
                        for (int i = 0; i < 20; i++) {
                            g.drawLine(80, 120, 50, 160);
                            g.drawLine(80, 120, 110, 160);
                            g.drawLine(80, 120, 80, 60);
                            g.drawOval(65, 30, 30, 30);
                            g.drawLine(80, 60, 110, 110);
                            g.drawLine(80, 60, 50, 110);
                            Thread.sleep(100);
                            //g.drawOval(40,40,i,i);
                            g.fillOval(80 - i / 2, 90 - i / 2, i, i * 2);
                            g.drawString("Ważysz więcej niż ostatnio",150,60);
                        }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        canvas.setSize(400, 300);
        ramka.add(canvas);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String name = e.getActionCommand();
        System.out.println(name);
        //Zamien switch na If
        switch (name) {
            case "Oblicz Bmi":
                //try-catch użyty został ponieważ chcę zabezpieczyć aplikację przed wprowadzeniem źle sformatowanych wartości np: gdyby ktoś wprowadził zamiast 1.80, E32,0
                try {
                    float heightTest = Float.parseFloat(height.getText());
                    int weightTest = Integer.parseInt(weight.getText());
                    int ageTest = Integer.parseInt(age.getText());
                } catch (NumberFormatException ec) {
                    description.setText(text.getString("nfe"));
                    return;
                }
                if (height.getText().isEmpty() | weight.getText().isEmpty() | age.getText().isEmpty()) {
                    description.setText(text.getString("empty"));
                    return;
                }
                /*if (Float.parseFloat(height.getText()) > 2.80 | Float.parseFloat(height.getText()) < 0.50) {
                    description.setText("Chyba został podany niepoprawyny wzrost...");
                    return;
                }
                if (Integer.parseInt(weight.getText()) > 400 | Integer.parseInt(weight.getText()) < 20) {
                    description.setText("Chyba została podana nieprawidłowa waga...");
                    return;
                }*/
                bmi = Integer.parseInt(weight.getText()) / (Float.parseFloat(height.getText()) * Float.parseFloat(height.getText()));
                result.setText("Twoje Bmi to: " + bmi);
                int lata = Integer.parseInt(age.getText());
                if (userGender.equals("Female")) {
                    if (lata > 15) {
                        Object[] wybor = {text.getString("no"), text.getString("yes")};
                        int i = JOptionPane.showOptionDialog(frame, text.getString("pregnant"), text.getString("question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, wybor, wybor[1]);
                        if (i == 1) {
                            result.setText("NaN");
                            description.setText(text.getString("pregnant_result"));
                            return;
                        }
                    }
                    if (bmi <= 18.4) {
                        description.setText(text.getString("underweight_f"));
                    } else if (bmi > 18.4 & bmi <= 24.9) {
                        description.setText(text.getString("normal_f"));
                    } else if (bmi > 24.9 & bmi <= 29.9) {
                        description.setText(text.getString("overweight_f"));
                    } else if (bmi > 29.9 & bmi <= 34.9) {
                        description.setText(text.getString("obesity_f"));
                    } else if (bmi > 34.9 & bmi <= 39.9) {
                        description.setText(text.getString("obesity_two_f"));
                    } else if (bmi > 39.9) {
                        description.setText(text.getString("obesity_giant_f"));
                    }
                } else if (userGender.equals("Male")) {
                    description.setText("Pana Bmi to: " + bmi);
                    if (bmi <= 18.4) {
                        description.setText(text.getString("underweight_m"));
                    } else if (bmi > 18.4 & bmi <= 24.9) {
                        description.setText(text.getString("normal_m"));
                    } else if (bmi > 24.9 & bmi <= 29.9) {
                        description.setText(text.getString("overweight_m"));
                    } else if (bmi > 29.9 & bmi <= 34.9) {
                        description.setText(text.getString("obesity_m"));
                    } else if (bmi > 34.9 & bmi <= 39.9) {
                        description.setText(text.getString("obesity_two_m"));
                    } else if (bmi > 39.9) {
                        description.setText(text.getString("obesity_giant_m"));
                    }
                }
                if (lata > 59 & lata < 120) {
                    if (userGender.equals("Female")) {
                        description.setText((text.getString("old_f")));

                    } else if (userGender.equals("Male")) {
                        description.setText((text.getString("old_m")));
                    }
                } else if (lata < 18) {
                    description.setText((text.getString("underage")));
                } /*else if (lata >= 120) {
                    description.setText("Chyba podany został trochę zawyżony wiek...");
                }*/
                files();
                break;
            case "Pokaż Wykres":
                JFrame chartFrame = new JFrame("Wykres");
                chartFrame.setLayout(new FlowLayout());
                JPanel first = new JPanel();
                chartFrame.setSize(frame.getWidth() - (frame.getWidth() / 4), frame.getHeight() - (frame.getHeight() / 4));
                chartFrame.setLocation(((int) dimension.getWidth() / 2) + frame.getWidth() / 2, (int) dimension.getHeight() / 2 - frame.getHeight() / 2);
                chartFrame.add(chart);
                chartFrame.add(first);
                charts();
                chart.setVisible(true);
                chartFrame.setVisible(true);
                break;
            case "Pokaż moje Dane":
                description.setText("A oto twoje dane do tej pory: ");
                description.setText(description.getText() + " \n");
                getData();
                for (int i = 0; i < dates.size(); i++) {
                    description.setText(description.getText() + " " + dates.get(i) + ": " + bmis.get(i) + "\n");
                }
                break;
            case "Wizualizuj postęp":
                Thread test = new Thread(this);
                test.start();
                break;
        }
    }
}
