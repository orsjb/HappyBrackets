package controller.test;

import java.util.Hashtable;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class FXCollectionsTest {

	public static void main(String[] args) {
		//I want to know if an ObservableList can keep an eye on an ObservableMap
		
		//create the ObservableMap
		ObservableMap<String, String> map = FXCollections.observableMap(new Hashtable<String, String>());
		//create the ObservableList looking at the values of the Map
		ObservableList<String> list = FXCollections.observableArrayList(map.values());
		
		//get the list to print out any changes that occur
		list.addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> arg0) {
				System.out.println(arg0);
			}
		});
		
		//now change something
		map.put("1", "ONE");
		map.put("2", "TWO");
	}

}
