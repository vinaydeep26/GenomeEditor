
import java.io.File;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class GenomeBuilder {

    public static void main(String[] args) {

        ArrayList<String> referenceList = new ArrayList<>();
        ArrayList<String> chromosomeList = new ArrayList<>();
        ArrayList<String> outputList = new ArrayList<>();
        
        try {
            String referenceString = "";
            Scanner scanner = new Scanner(new File("reference.fasta"));
            while (scanner.hasNext()) {
                String row = scanner.nextLine();
                if (!row.contains(">")) {
                    referenceString += row;
                } else {
                    chromosomeList.add(row.replace(">", ""));
                    if (referenceString.length() != 0) {
                        referenceList.add(referenceString);
                        referenceString = "";
                    }
                }
            }
            referenceList.add(referenceString);

            int index = 0, differenceInLength = 0;
            String outputString = "", prevChrVal = chromosomeList.get(index);

            scanner = new Scanner(new File("input.vcf"));
            while (scanner.hasNext()) {
                String row = scanner.nextLine();
                if (row.charAt(0) != '#') {
                    String columns[] = row.split("\t");
                    if (columns.length == 10 && !columns[9].startsWith("0/0")) {

                        String chrVal = columns[0];
                        index = chromosomeList.indexOf(chrVal);

                        if (index != -1) {
                            if (!chrVal.equals(prevChrVal)) {
                                prevChrVal = chrVal;
                                outputList.add(outputString);
                                outputString = "";
                                differenceInLength = 0;
                            }

                            int position = (Integer.parseInt(columns[1]) - 1) + differenceInLength;
                            if (position <= referenceList.get(index).length()) {

                                outputString = referenceList.get(index).substring(0, (position));

                                if (columns[4].contains(",")) {
                                    columns[4] = columns[4].substring(0, columns[4].indexOf(","));
                                }

                                int refLength = columns[3].length();
                                int altLength = columns[4].length();
                                differenceInLength += (altLength - refLength);
                                outputString += columns[4];
                                outputString += referenceList.get(index).substring((position
                                        + refLength), referenceList.get(index).length());
                                referenceList.set(index, outputString);
                            }
                        }
                    }
                }
            }
            outputList.add(outputString);

            FileWriter filewriter = new FileWriter(new File("output.fasta"));

            for (int i = 0; i < chromosomeList.size(); i++) {
                filewriter.write(">" + chromosomeList.get(i) + "\n");
                int k = 100;
                for (int j = 0; j < outputList.get(i).length(); j += 100) {
                    if (k <= outputList.get(i).length()) {
                        filewriter.write(outputList.get(i).substring(j, k) + "\n");
                        k += 100;
                    } else {
                        filewriter.write(outputList.get(i).substring(j, outputList.get(i).length()) + "\n");
                    }
                }
            }
            scanner.close();
            filewriter.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }

    }
}
