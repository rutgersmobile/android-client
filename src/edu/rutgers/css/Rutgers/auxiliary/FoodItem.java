package edu.rutgers.css.Rutgers.auxiliary;


/**
 * Class for holding food menu item/category data
 *
 */
public class FoodItem {

	public String title;
	public boolean isCategory;
	public int calories;
	public String serving;
	public String[] ingredients;
	
	/**
	 * Default constructor. Item is assumed to not be a category.
	 * @param title Menu item title
	 */
	public FoodItem(String title, int calories, String serving, String[] ingredients) {
		this.title = title;
		this.calories = calories;
		this.serving = serving;
		this.ingredients = ingredients;
		this.isCategory = false;
	}

	/**
	 * Constructor that allows category flag to be set
	 * @param title Category or menu item title
	 * @param isCategory Is this item a category?
	 */
	public FoodItem(String title, boolean isCategory) {
		this.title = title;
		this.isCategory = isCategory;
	}
	
	/*
	 * Getters & setters
	 */
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title; 
	}
	
	public boolean getIsCategory() {
		return this.isCategory;
	}
	
	public void setIsCategory(boolean isCategory) {
		this.isCategory = isCategory;
	}
	
	/**
	 * Returns food item title
	 * @return Food item title
	 */
	public String toString() {
		return this.title;
	}
	
}
