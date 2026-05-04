/**
 * Copyright (c) 2025 Sami Menik, PhD. All rights reserved.
 * 
 * Unauthorized copying of this file, via any medium, is strictly prohibited.
 * This software is provided "as is," without warranty of any kind.
 */
package cs2725.examples;

import java.io.File;
import java.util.Comparator;

import cs2725.api.List;
import cs2725.api.Set;
import cs2725.api.df.ColumnAggregate;
import cs2725.api.df.DataFrame;
import cs2725.api.df.Series;
import cs2725.impl.df.DataFrameImpl;

/**
 * This class demonstrates the completed DataFrame project using the movies dataset.
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

        // -----------------------------------------------------------------------
        // PART 1: Top 10 highest rated movies by average rating
        // -----------------------------------------------------------------------

        // Define a column aggregate to calculate average rating per movie
        ColumnAggregate<Integer, Double> avgRatingAgg = new ColumnAggregate<>(
                "Rating", "AvgRating",
                (g) -> g.mean(),
                Integer.class, Double.class);

        // Group by Title and aggregate to get average rating per movie
        DataFrame movieAvgRatings = dataFrame
                .groupBy("Title")
                .aggregate(List.of(avgRatingAgg));

        // Sort by AvgRating descending to get highest rated first
        movieAvgRatings = movieAvgRatings.sortBy("AvgRating", Comparator.reverseOrder(), Double.class);

        // Take the top 10
        DataFrame top10Movies = movieAvgRatings.rows(0, 10);

        // Keep only Title and AvgRating columns
        DataFrame top10Result = top10Movies.columns(List.of("Title", "AvgRating"));

        System.out.println("=== Top 10 Highest Rated Movies (by Average Rating) ===");
        System.out.println(top10Result);


        // Get the set of top 10 movie titles
        Series<String> top10Titles = top10Movies.getColumn("Title", String.class);
        Set<String> top10TitlesSet = top10Titles.unique();

        // Filter original dataframe to only rows where Title is in top 10
        Series<Boolean> top10Mask = dataFrame.getColumn("Title", String.class)
                .mapValues((title) -> top10TitlesSet.contains(title));
        DataFrame top10Data = dataFrame.selectByMask(top10Mask);

        // Create a combined column: "Title (Year) | AgeGroup"
        // This makes groupBy work on (title, age) pairs
        Series<String> titleSeries = top10Data.getColumn("Title", String.class);
        Series<String> ageSeries = top10Data.getColumn("Age", String.class);

        Series<String> combinedSeries = titleSeries.combineWith(ageSeries,
                (title, age) -> title + " | " + age);

        // Add the combined column to the dataframe
        DataFrame top10WithCombined = top10Data.addColumn("TitleAge", String.class, combinedSeries);

        // Define aggregates for the group-by
        ColumnAggregate<Integer, Double> ageGroupAvgRating = new ColumnAggregate<>(
                "Rating", "AvgRating",
                (g) -> g.mean(),
                Integer.class, Double.class);

        // Group by the combined TitleAge column and aggregate
        DataFrame top10ByAge = top10WithCombined
                .groupBy("TitleAge")
                .aggregate(List.of(ageGroupAvgRating));

        // Sort by title+age for readability
        top10ByAge = top10ByAge.sortBy("TitleAge", Comparator.naturalOrder(), String.class);

        // Sort alphabetically for readability
        top10ByAge = top10ByAge.sortBy("TitleAge", Comparator.naturalOrder(), String.class);

        System.out.println("=== Part 2: Top 10 Movies - Average Rating by Age Group ===");
        System.out.println(top10ByAge);

        // -----------------------------------------------------------------------
        // EXTRA CREDIT: Rating patterns by Gender and Age group (two groupBy ops)
        // -----------------------------------------------------------------------

        // GroupBy 1: Group by Genre -> overall avg rating per genre
        ColumnAggregate<Integer, Double> genreAvg = new ColumnAggregate<>(
                "Rating", "AvgRating",
                (g) -> g.mean(),
                Integer.class, Double.class);

        DataFrame genreRatings = dataFrame
                .groupBy("Genres")
                .aggregate(List.of(genreAvg));

        genreRatings = genreRatings.sortBy("AvgRating", Comparator.reverseOrder(), Double.class);
        DataFrame top5Genres = genreRatings.rows(0, 5);

        System.out.println("=== Extra Credit Step 1: Top 5 Genres by Overall Avg Rating ===");
        System.out.println(top5Genres);

        // GroupBy 2: Group by Gender+Age combined -> avg rating per demographic group
        Series<String> genderSeries = dataFrame.getColumn("Gender", String.class);
        Series<String> ageCol = dataFrame.getColumn("Age", String.class);
        Series<String> genderAgeCombined = genderSeries.combineWith(ageCol,
                (gender, age) -> gender + " | " + age);

        DataFrame dfWithGenderAge = dataFrame.addColumn("GenderAge", String.class, genderAgeCombined);

        ColumnAggregate<Integer, Double> genderAgeAvg = new ColumnAggregate<>(
                "Rating", "AvgRating",
                (g) -> g.mean(),
                Integer.class, Double.class);

        DataFrame ratingsByGenderAge = dfWithGenderAge
                .groupBy("GenderAge")
                .aggregate(List.of(genderAgeAvg));

        ratingsByGenderAge = ratingsByGenderAge.sortBy("AvgRating", Comparator.reverseOrder(), Double.class);

        System.out.println("=== Extra Credit Step 2: Avg Rating by Gender + Age Group ===");
        System.out.println(ratingsByGenderAge);
    }
}
