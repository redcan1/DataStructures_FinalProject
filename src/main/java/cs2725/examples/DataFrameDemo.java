/**
 * Copyright (c) 2025 Sami Menik, PhD. All rights reserved.
 * 
 * Unauthorized copying of this file, via any medium, is strictly prohibited.
 * This software is provided "as is," without warranty of any kind.
 */
package cs2725.examples;

import java.io.File;
import cs2725.api.df.DataFrame;
import cs2725.impl.df.DataFrameImpl;

/**
 * This class must be used to demonstrate the completed DataFrame project.
 */
public class DataFrameDemo {

    public static void main(String[] args) {
        // Load data from a CSV file. After loading all columns are of type String.
        DataFrame dataFrame = new DataFrameImpl()
                .readCsv("resources" + File.separator + "movies.csv");

        // Convert the String columns to their appropriate types as needed.
        dataFrame = dataFrame.mapValues("Month", (v) -> Integer.parseInt(v), String.class, Integer.class)
                .mapValues("Day", (v) -> Integer.parseInt(v), String.class, Integer.class)
                .mapValues("Year", (v) -> Integer.parseInt(v), String.class, Integer.class)
                .mapValues("Rating", (v) -> Integer.parseInt(v), String.class, Integer.class);

        // Todo: Project 2: To be implemented
        // Implement the demo according to the instructions in the project description.
        throw new UnsupportedOperationException("To be implemented...");
    }

}
