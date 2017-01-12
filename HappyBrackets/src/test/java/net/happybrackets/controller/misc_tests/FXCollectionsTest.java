/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.controller.misc_tests;

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
		
		//getInstance the list to print out any changes that occur
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
