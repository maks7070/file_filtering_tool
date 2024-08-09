import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public class MainPage extends JFrame {



    private JTextField sourceFolderField;
    private JComboBox<String> fileTypeComboBox;
    private JSpinner maxSizeField;
    private JTextField destinationFolderField;

    public MainPage(){
        // Set up the frame
        setTitle("File Filter");
        setSize(600, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10,10,10,10);
        gbc.weightx = 1.0;

        // Row 0: Source Folder
        JLabel sourceFolderLabel = new JLabel("Source Folder:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(sourceFolderLabel, gbc);

        sourceFolderField = new JTextField(100);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        add(sourceFolderField, gbc);

        JButton sourceFolderButton = new JButton("Browse...");
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(sourceFolderButton, gbc);


        JLabel fileExtensionLabel = new JLabel("File Extension:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(fileExtensionLabel, gbc);

        fileTypeComboBox = new JComboBox<>();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(fileTypeComboBox, gbc);


        JLabel maxSizeLabel = new JLabel("Max File Size (MB):");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(maxSizeLabel, gbc);

        maxSizeField = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(maxSizeField, gbc);


        JLabel destinationFolderLabel = new JLabel("Destination Folder:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        add(destinationFolderLabel, gbc);

        destinationFolderField = new JTextField(25);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        add(destinationFolderField, gbc);

        JButton destinationFolderButton = new JButton("Browse...");
        gbc.gridx = 2;
        gbc.gridy = 3;
        add(destinationFolderButton, gbc);


        JButton submitButton = new JButton("Submit");
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        add(submitButton, gbc);



        sourceFolderButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    File folder = chooser.getSelectedFile();
                    sourceFolderField.setText(folder.getAbsolutePath());

                    populateFileExtensionTypes(folder);
                }
            }
        });

        destinationFolderButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = chooser.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION){
                    File folder = chooser.getSelectedFile();
                    destinationFolderField.setText(folder.getAbsolutePath());
                }
            }
        });


        submitButton.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               String sourceFolder = sourceFolderField.getText();
               String fileExtension = (String) fileTypeComboBox.getSelectedItem();
               int maxSize = (Integer) maxSizeField.getValue();
               String destinationFolder = destinationFolderField.getText();

               File srcDir = new File(sourceFolder);
               File destDir = new File(destinationFolder);

               if(!srcDir.isDirectory() || !destDir.isDirectory()){
                   JOptionPane.showMessageDialog(null, "Invalid source or destination folder");
                   return;
               }

               String newFolderName = "Filtered_Files_" + System.currentTimeMillis();
               File newDestFolder = new File(destDir, newFolderName);
               if(!newDestFolder.mkdirs()){
                   JOptionPane.showMessageDialog(null, "Failed to create destination folder");
                   return;
               }

               File []files = srcDir.listFiles((dir, name) -> name.endsWith(fileExtension));
               if(files != null && files.length > 0){
                   int movedFiles = 0;
                   for(File file : files){
                       if(file.isFile() && file.length() <= (long) maxSize * 1024 * 1024){
                           try{
                               Path destPath = new File(newDestFolder, file.getName()).toPath();
                               Files.move(file.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                               movedFiles++;
                           }catch(Exception ex){
                               JOptionPane.showMessageDialog(null, "Error moving file: " + file.getName());

                           }
                       }
                   }
                   JOptionPane.showMessageDialog(null, movedFiles + " file(s) filtered and moved successfully!" );

               }else{
                   JOptionPane.showMessageDialog(null, "No files found!");

               }
           }
        });



    }

    private void populateFileExtensionTypes(File folder){
        Set<String> extensions = new HashSet<>();
        File[] files = folder.listFiles();

        if(files != null){
            for(File file : files){
                if(file.isFile()){
                    String name = file.getName();
                    int dotIndex = name.lastIndexOf(".");
                    if(dotIndex > 0 && dotIndex < name.length() - 1){
                        extensions.add(name.substring(dotIndex));
                    }
                }
            }
        }
        fileTypeComboBox.removeAllItems();
        for(String ext: extensions){
            fileTypeComboBox.addItem(ext);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainPage ui = new MainPage();
            ui.setVisible(true);
        });
    }
}
