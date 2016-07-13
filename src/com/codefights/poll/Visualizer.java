package com.codefights.poll;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by dmitry on 02.07.16.
 */
class Visualizer {
    private final static int WINDOW_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private final static int WINDOW_HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private final static int BUTTON_HEIGHT = 20;
    private final static int DEFAULT_VERTEX_SIZE_X = 150;
    private final static int DEFAULT_VERTEX_SIZE_Y = 20;

    private class PollButton extends JButton {
        PollButton(String text, int width, int height) {
            setSize(width, height);
            setText(text);
            setActionCommand(text);
            setVisible(true);
        }
    }

    private class TreeVertex extends JButton implements MouseMotionListener, MouseListener {
        private boolean isSelected;
        private MainFrame frame;
        private boolean dragging;
        private MouseEvent lastPress;
        private int sizeX; //Sizes used for drag-n-drop
        private int sizeY;
        private int x; //Cordinates that may be changed with drag-n-drop
        private int y;

        TreeVertex(MainFrame frame, String name, int x, int y) {
            super();
            this.frame = frame;
            this.dragging = false;
            this.isSelected = false;
            this.sizeX = DEFAULT_VERTEX_SIZE_X;
            this.sizeY = DEFAULT_VERTEX_SIZE_Y;
            this.x = x;
            this.y = y;
            setText(name);
            setBounds(x, y, sizeX, sizeY);
            revalidate();
            repaint();
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }

        int getx() {
            return x;
        }

        int gety() {
            return y;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastPress = e;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dragging = false;
            lastPress = null;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            dragging = true;
            int dx = e.getX() - lastPress.getX();
            int dy = e.getY() - lastPress.getY();
            x += dx;
            y += dy;
            setBounds(x, y, sizeX, sizeY);
            repaint();
            frame.showTree(frame.currentPKT);
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            /*FontMetrics metrics = g.getFontMetrics();
            int width = metrics.stringWidth(name);
            int height = BUTTON_HEIGHT;
            System.out.println(name + " " + width + " " + height);
            this.sizeX = width;
            this.sizeY = height;
            setBounds(this.x, this.y, this.sizeX, this.sizeY);
            setText(name);*/
        }
    }

    private class Line {
        int fromX;
        int fromY;
        int toX;
        int toY;

        Line(int fromX, int fromY, int toX, int toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }
    }

    private class DrawPanel extends JPanel {
        private ArrayList<Line> linesToDraw = new ArrayList<>();
        private String error;

        void addLine(Line line) {
            linesToDraw.add(line);
        }

        void setError(String error) {
            this.error = error;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Line line : linesToDraw) {
                g.drawLine(line.fromX, line.fromY, line.toX, line.toY);
            }
            int TEXT_WIDTH = getWidth() - 50;
            int TEXT_HEIGHT = 100;
            g.clearRect(getWidth() - TEXT_WIDTH, getHeight() - TEXT_HEIGHT, TEXT_WIDTH, TEXT_HEIGHT);
            g.setColor(Color.red);
            g.setFont(new Font("Times New Roman", Font.PLAIN, 20));
            if (error != null) {
                g.drawString(error, getWidth() - TEXT_WIDTH, getHeight() - TEXT_HEIGHT);
            }
        }
    }

    private class MainFrame extends JFrame {
        private String currentTask;
        private JTextArea descriptionArea;
        private JScrollPane descriptionPane;
        private JPanel rightPane;
        private JPanel optionsPane;
        private DrawPanel treePane;
        private JSplitPane pane;
        private PollButton[] trees;
        private PollButton submitButton;
        private PollButton backButton;
        private boolean submitClicked = false;
        private boolean backClicked = false;
        private PKT[] pkts;
        private PKT currentPKT;
        private GroupLayout layout;
        private HashMap<PKT, ArrayList<TreeVertex>> vertices;
        private HashMap<PKT, HashMap<TreeVertex, ArrayList<TreeVertex>>> treeEdges;
        private String[] selected;

        MainFrame() throws ConfigException {
            setTitle("Classification poll");
            setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            Dimension minimalSize = new Dimension(WINDOW_WIDTH / 5, WINDOW_HEIGHT);
            Dimension preferredSize = new Dimension(WINDOW_WIDTH / 2, WINDOW_HEIGHT);

            descriptionArea = new JTextArea(WINDOW_WIDTH / 2, WINDOW_HEIGHT);
            descriptionArea.setFont(new Font("Times New Roman", Font.BOLD, 14));
            descriptionPane = new JScrollPane(descriptionArea);
            descriptionPane.setMinimumSize(minimalSize);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionPane.setPreferredSize(preferredSize);
            add(descriptionPane, BorderLayout.CENTER);

            rightPane = new JPanel();
            rightPane.setSize(WINDOW_WIDTH / 2, WINDOW_HEIGHT);
            rightPane.setPreferredSize(preferredSize);
            rightPane.setMinimumSize(minimalSize);

            optionsPane = new JPanel();
            optionsPane.setSize(WINDOW_WIDTH / 2, BUTTON_HEIGHT * 2);
            optionsPane.setPreferredSize(new Dimension(WINDOW_WIDTH / 2, BUTTON_HEIGHT * 2));
            optionsPane.setMinimumSize(new Dimension(WINDOW_WIDTH / 2, BUTTON_HEIGHT * 2));

            treePane = new DrawPanel();
            treePane.setSize(WINDOW_WIDTH / 2, WINDOW_HEIGHT - BUTTON_HEIGHT * 2);
            treePane.setPreferredSize(new Dimension(WINDOW_WIDTH / 2, WINDOW_HEIGHT - BUTTON_HEIGHT * 2));
            treePane.setMinimumSize(new Dimension(WINDOW_WIDTH / 2, WINDOW_HEIGHT - BUTTON_HEIGHT * 2));

            rightPane.add(optionsPane, BorderLayout.PAGE_START);
            rightPane.add(treePane, BorderLayout.CENTER);

            pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, descriptionPane, rightPane);
            pane.setOneTouchExpandable(true);
            pane.setDividerLocation(WINDOW_WIDTH / 2);
            this.getContentPane().add(pane);

            Config config = Config.loadPKT();
            assert config != null;
            trees = new PollButton[config.roots.size()];
            pkts = new PKT[config.roots.size()];
            int buttonWidth = pane.getRightComponent().getWidth() / (trees.length + 2);
            int buttonHeight = BUTTON_HEIGHT;
            for (int i = 0; i < config.roots.size(); i++) {
                pkts[i] = new PKT(config, i);
                trees[i] = new PollButton(pkts[i].getRoot(), buttonWidth, buttonHeight);
            }
            submitButton = new PollButton("submit", buttonWidth, buttonHeight);
            backButton = new PollButton("back", buttonWidth, buttonHeight);

            layout = new GroupLayout(optionsPane);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);
            GroupLayout.SequentialGroup group = layout.createSequentialGroup();
            group.addComponent(backButton);
            for (PollButton button : trees) {
                group.addComponent(button);
            }
            group.addComponent(submitButton);
            layout.setHorizontalGroup(group);

            vertices = new HashMap<>();
            treeEdges = new HashMap<>();
            currentPKT = pkts[0];
            showTree(currentPKT);
            for (int i = 0; i < trees.length; i++) {
                final int fi = i;
                trees[i].addActionListener(event -> {
                    currentPKT = pkts[fi];
                    showTree(pkts[fi]);
                });
            }
            backButton.addActionListener(event -> backClicked = true);
            submitButton.addActionListener(event -> {
                ArrayList<String> selected = new ArrayList<>();
                for (TreeVertex vertex : vertices.get(currentPKT)) {
                    if (vertex.isSelected) {
                        selected.add(vertex.getText());
                    }
                }
                this.selected = selected.toArray(new String[selected.size()]);
                String checkResult = currentPKT.checkSelected(this.selected, "general");
                Storage storage = new Storage();
                if (checkResult == null || (this.selected.length == 0 && storage.haveThemes(currentTask))) {
                    if (this.selected.length == 0 && storage.haveThemes(currentTask)) {
                        List<String> tmp = storage.getThemes(currentTask);
                        this.selected = tmp.toArray(new String[tmp.size()]);
                    }
                    submitClicked = true;
                    for (TreeVertex vertex : vertices.get(currentPKT)) {
                        vertex.isSelected = false;
                        vertex.setBackground(null);
                    }
                    treePane.setError(null);
                    repaint();
                } else {
                    treePane.setError(checkResult);
                    repaint();
                }
            });

            setVisible(true);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }

        private void drawEdge(TreeVertex from, TreeVertex to) {
            drawEdge(from, to.getx() + to.sizeX / 2, to.gety());
        }

        private void drawEdge(TreeVertex from, int toX, int toY) {
            int fromX = from.getX() + from.sizeX / 2;
            int fromY = from.getY() + from.sizeY;
            treePane.addLine(new Line(fromX, fromY, toX, toY));
        }

        private void drawTree(PKT tree, String vertex, TreeVertex parent, int minWidth, int maxWidth, int depth) {
            ArrayList<String> children = tree.getChildren(vertex);
            TreeVertex button = new TreeVertex(this, vertex, (minWidth + maxWidth) / 2, 2 * BUTTON_HEIGHT * (1 + depth));
            button.addActionListener(event -> {
                button.isSelected ^= true;
                if (button.isSelected) {
                    button.setBackground(Color.yellow);
                } else {
                    button.setBackground(null);
                }
            });
            if (parent != null) {
                treeEdges.get(currentPKT).get(parent).add(button);
            }
            treeEdges.get(currentPKT).put(button, new ArrayList<>());
            vertices.get(currentPKT).add(button);
            if (children.size() == 0) {
                return;
            }
            int diff = (maxWidth - minWidth) / children.size();
            for (int i = 0; i < children.size(); i++) {
                drawTree(tree, children.get(i), button, minWidth + diff * i, minWidth + diff * (i + 1), depth + 1);
            }
        }

        private void drawKnownTree() {
            for (TreeVertex vertex : vertices.get(currentPKT)) {
                treePane.add(vertex);
            }
            for (TreeVertex from : treeEdges.get(currentPKT).keySet()) {
                for (TreeVertex to : treeEdges.get(currentPKT).get(from)) {
                    drawEdge(from, to);
                }
            }
        }

        private void showTree(PKT tree) {
            treePane.removeAll();
            treePane.linesToDraw.clear();
            treePane.revalidate();
            treePane.repaint();
            treePane.setLayout(null);
            if (vertices.get(currentPKT) == null) {
                vertices.put(currentPKT, new ArrayList<>());
                treeEdges.put(currentPKT, new HashMap<>());
                drawTree(tree, tree.getRoot(), null, 0, treePane.getWidth() - 100, 1);
            }
            drawKnownTree();
        }
    }

    private MainFrame mainFrame;

    Visualizer() {
        try {
            mainFrame = new MainFrame();
        } catch (ConfigException e) {
            e.printStackTrace();
        }
    }

    String[] loadTask(File file, boolean onlyGeneralSelection) {
        File descriptionFile = new File(file, "README.md");
        if (!descriptionFile.exists()) {
            System.err.println("There is not README.md for task " + file.toString());
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(descriptionFile))) {
            ArrayList<String> lines = new ArrayList<>();
            lines.addAll(reader.lines().collect(Collectors.toList()));

            mainFrame.currentTask = file.getName();

            String markdownSource = String.join("\n", lines);
            //String htmlSource = new Markdown4jProcessor().process(markdownSource);
            mainFrame.descriptionArea.setText(markdownSource);
            //TODO: make the text be rendered markdown

            while (!mainFrame.submitClicked && !mainFrame.backClicked) {
                Thread.sleep(200);
            }
            if (mainFrame.backClicked) {
                mainFrame.backClicked = false;
                return new String[]{null};
            }
            mainFrame.submitClicked = false;
            return mainFrame.selected;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
