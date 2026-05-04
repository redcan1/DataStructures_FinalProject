/**
 * Copyright (c) 2025 Sami Menik, PhD. All rights reserved.
 * 
 * Unauthorized copying of this file, via any medium, is strictly prohibited.
 * This software is provided "as is," without warranty of any kind.
 */
package cs2725.impl.df;
 
import cs2725.api.List;
import cs2725.api.Map;
import cs2725.api.df.Series;
import cs2725.api.df.SeriesGroupBy;
import cs2725.api.functional.AggregateFunction;
import cs2725.impl.ArrayList;
import cs2725.impl.HashMap;
import cs2725.impl.ImmutableList;
 
/**
 * Implementation of SeriesGroupBy.
 *
 * @param <T> the type of elements in the original Series
 */
public class SeriesGroupByImpl<T> implements SeriesGroupBy<T> {
 
    private final Series<T> series;
    private final Map<T, List<Integer>> groups;
    private final List<T> index;
 
    public SeriesGroupByImpl(Series<T> series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null.");
        }
        this.series = series;
        this.groups = groupByValues();
        this.index = buildIndex();
    }
 
    /**
     * Groups elements by their values, creating a map from unique values to index
     * positions.
     */
    private Map<T, List<Integer>> groupByValues() {
        Map<T, List<Integer>> result = new HashMap<>();
 
        for (int i = 0; i < series.size(); i++) {
            T value = series.get(i);
 
            // If this value hasn't been seen yet, create a new list for it
            if (!result.containsKey(value)) {
                result.put(value, new ArrayList<>());
            }
 
            // Add the current position to this value's list
            result.get(value).insertItem(i);
        }
 
        return result;
    }
 
    /**
     * Builds the index of unique group values in the order they first appear.
     */
    private List<T> buildIndex() {
        List<T> uniqueValues = new ArrayList<>(groups.size());
        for (T key : groups.keySet()) {
            uniqueValues.insertItem(key);
        }
        return uniqueValues;
    }
 
    @Override
    public Series<T> series() {
        return series;
    }
 
    @Override
    public Map<T, List<Integer>> groups() {
        // Return a copy to ensure immutability.
        Map<T, List<Integer>> copy = new HashMap<>(groups.size(), 0.75);
        for (T key : groups.keySet()) {
            List<Integer> indicesImmutable = ImmutableList.of(groups.get(key));
            copy.put(key, indicesImmutable);
        }
        return copy;
    }
 
    @Override
    public List<T> index() {
        return ImmutableList.of(index);
    }
 
    @Override
    public <R> Series<R> aggregate(AggregateFunction<T, R> aggregator) {
        if (aggregator == null) {
            throw new IllegalArgumentException("Aggregator cannot be null.");
        }
 
        List<R> aggregatedValues = new ArrayList<>(index.size());
 
        // For each unique group value (in index order)
        for (T key : index) {
            // Get the list of positions for this group
            List<Integer> indices = groups.get(key);
 
            // Build a sub-series containing only elements in this group
            Series<T> groupSeries = series.withIndex(indices);
 
            // Apply the aggregator to the sub-series and store the result
            R result = aggregator.apply(groupSeries);
            aggregatedValues.insertItem(result);
        }
 
        return new SeriesImpl<>(aggregatedValues);
    }
}
 