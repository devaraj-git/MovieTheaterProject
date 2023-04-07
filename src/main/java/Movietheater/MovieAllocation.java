package Movietheater;

import com.sun.jdi.Value;

import java.util.*;
import java.util.stream.Collectors;

public class MovieAllocation {
    public static void main(String[] args) {
        // Initialize input data
        int[] Movie_Showtime = {1200, 1330, 1400, 1500, 1600}; // movie showtimes in 24-hour format
        int[] no_of_screens = {1000, 1300, 1430, 1600}; // available time slots in 24-hour format
        int[] rating_score = {4, 2, 3, 5, 1}; // popularity ratings of the movies
        // Allocate no_of_screens to screens based on popularity and distance
        Map<Integer, List<Integer>> allocation = allocateMoviesByPopularityAndDistance(Movie_Showtime, no_of_screens, rating_score);
        // Handle scheduling conflicts using minimum swaps algorithm
        int minSwaps = minimumSwaps(allocation);
        System.out.println("Minimum swaps to resolve scheduling conflicts: " + minSwaps);
        // Optimize movie showing timing and duration using strange printer algorithm
        optimizeMovieTimingAndDuration(allocation);
        // Rank user reviews based on beauty score
        String[] reviews = {"This movie was fantastic!", "I didn't like this movie at all.", "Average movie, nothing special."};
        int[] beautyScores = rankReviewsByBeautyScore(reviews);
        for (int i = 0; i < reviews.length; i++) {
            System.out.println("Review " + (i + 1) + " beauty score: " + beautyScores[i]);
        }
        // Print final allocation
        System.out.println("\nFinal movie allocation:");
        for (Map.Entry<Integer, List<Integer>> entry : allocation.entrySet()) {
            int screen = entry.getKey();
            List<Integer> movieShowtimes = entry.getValue();
            System.out.println("Screen ------>" + screen + ": " + movieShowtimes);
        }
    }

    public static Map<Integer, List<Integer>> allocateMoviesByPopularityAndDistance(int[] Movie_Showtime, int[] no_of_screens, int[] rating_score) {
        // Initialize output data
        Map<Integer, List<Integer>> allocation = new HashMap<>();
        for (int screen : no_of_screens) {
            allocation.put(screen, new ArrayList<>());
        }
        // Sort movies and screens in ascending order
        Arrays.sort(Movie_Showtime);
        Arrays.sort(no_of_screens);
        // Sortmovies by popularity in descending order

        Integer[] movieIndices = new Integer[Movie_Showtime.length];
        for (int i = 0; i < Movie_Showtime.length; i++) {
            movieIndices[i] = i;
        }
        Arrays.sort(movieIndices, (a, b) -> Integer.compare(rating_score[b], rating_score[a]));
        // Allocate Movie_Showtime to screens based on minimizing the total distance
        int movieIndex = 0;
        for (int screen : no_of_screens) {
            int minDistance = Integer.MAX_VALUE;
            int closestMovieIndex = -1;
            for (int i = movieIndex; i < Movie_Showtime.length; i++) {
                int movie = Movie_Showtime[movieIndices[i]];
                int distance = Math.abs(screen - movie);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestMovieIndex = i;
                }
            }
            int closestMovie = Movie_Showtime[movieIndices[closestMovieIndex]];
            allocation.get(screen).add(closestMovie);
            movieIndex = closestMovieIndex + 1;
            if (movieIndex >= Movie_Showtime.length) {
                break;
            }
        }
        System.out.println("********----------- > "+ allocation);
        return allocation; //mapping of screen numbers to the list of showtimes
    }
    private static int[] rankReviewsByBeautyScore(String[] reviews) {
        int[] beautyScores = new int[reviews.length];
        for (int i = 0; i < reviews.length; i++) {
            String review = reviews[i];
            int[] frequency = new int[26];
            for (int j = 0; j < review.length(); j++) {
                char c = review.charAt(j);
                if (c >= 'a' && c <= 'z') {
                    frequency[c - 'a']++;
                } else if (c >= 'A' && c <= 'Z') {
                    frequency[c - 'A']++;
                }
            }
            Arrays.sort(frequency);
            int score = 0;
            for (int j = frequency.length - 1, k = 26; j >= 0; j--, k--) {
                score += frequency[j] * k;
            }
            beautyScores[i] = score;
        }
        return beautyScores;
    }
//
    private static void optimizeMovieTimingAndDuration(Map<Integer, List<Integer>> allocation) {
        System.out.println("----- Allocation ------" + allocation.values());
        List<Integer> movieShowtimes1=allocation.values().stream().flatMap(list -> list.stream()).collect(Collectors.toList());
        System.out.println("----- movieshow1 times list ------" +movieShowtimes1);
        for (Integer movieShowtimeslist : movieShowtimes1) {
            List<Integer> movieShowtimes = new ArrayList<>();
            movieShowtimes.add(movieShowtimeslist);
            System.out.println("------------ movieshowtimes >" + movieShowtimes);
            int start = movieShowtimes.get(0); // error here
            System.out.println(" Start  -> " + start);
            int end = (int) movieShowtimes.get(movieShowtimes.size() - 1);
            int[][] dp = new int[end - start + 1][end - start + 1];
            for (int len = 1; len <= end - start + 1; len++) {
                for (int i = 0; i <= end - start - len + 1; i++) {
                    int j = i + len - 1;
                    if (len == 1) {
                        dp[i][j] = 1;
                    } else {
                        dp[i][j] = dp[i + 1][j] + 1;
                        for (int k = i + 1; k <= j; k++) {
                            if (movieShowtimes.contains(start + i + k - 1)) {
                                dp[i][j] = Math.min(dp[i][j], dp[i][k - 1] + dp[k][j] - 1);
                            } else {
                                dp[i][j] = Math.min(dp[i][j], dp[i][k - 1] + dp[k][j]);
                            }
                        }
                    }
                }
            }
            List<Integer> newShowtimes = new ArrayList<>();
            int i = 0;
            int j = end - start;
            while (i <= j) {
                if (movieShowtimes.contains(start + i)) {
                    newShowtimes.add(start + i);
                    i++;
                } else if (movieShowtimes.contains(start + j)) {
                    newShowtimes.add(start + j);
                    j--;
                } else {
                    if (dp[i + 1][j] + 1 == dp[i][j]) {
                        i++;
                    } else {
                        j--;
                    }
                }
            }
            movieShowtimes.clear();
            movieShowtimes.addAll(newShowtimes);
        }


    }

    private static int minimumSwaps(Map<Integer, List<Integer>> allocation) {
        int numSwaps = 0;
        List<Integer> showtimes = new ArrayList<>();
        for (List<Integer> movieShowtimes : allocation.values()) {
            showtimes.addAll(movieShowtimes);
        }
        int n = showtimes.size();
        int[] idx = new int[n];
        for (int i = 0; i < n; i++) {
            idx[i] = i;
        }
        // sort the indices based on the showtimes
        Arrays.sort(idx);
        Comparator.comparingInt(showtimes::get);
        // use a modified bubble sort to count the number of swaps required
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (idx[i] > idx[j]) {
                    numSwaps++;
                    int temp = idx[i];
                    idx[i] = idx[j];
                    idx[j] = temp;
                }
            }
        }
        return numSwaps;
    }


}

