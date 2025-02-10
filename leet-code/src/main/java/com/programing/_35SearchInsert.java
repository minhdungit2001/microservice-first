package com.programing;

public class _35SearchInsert {
    public int searchInsert(int[] nums, int target) {
        int result = -1;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == target) {
                result = i;
                break;
            }
            if (nums[i] < target) {
                result = i + 1;
            }

        }
        return result;
    }

    public static void main(String[] args) {

        int searchInsert = new _35SearchInsert().searchInsert(new int[]{1, 3, 5, 6}, 4);
        System.out.println("==========Result: " + searchInsert);
    }

}
