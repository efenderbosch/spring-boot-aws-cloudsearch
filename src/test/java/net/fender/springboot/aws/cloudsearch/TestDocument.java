package net.fender.springboot.aws.cloudsearch;

import java.time.ZonedDateTime;
import java.util.List;

import net.fender.springboot.aws.cloudsearch.docs.AddDocument;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public class TestDocument {

	@JsonProperty("list_of_integers_is")
	private List<Integer> listOfIntegers;
	@JsonProperty("literal_l")
	private String literal;
	@JsonProperty("integer_i")
	private Integer integer;
	@JsonProperty("date_dt")
	private ZonedDateTime date;
	@JsonProperty("list_of_literals_ls")
	private List<String> listOfLiterals;

	public List<Integer> getListOfIntegers() {
		return listOfIntegers;
	}

	public void setListOfIntegers(List<Integer> listOfIntegers) {
		this.listOfIntegers = listOfIntegers;
	}

	@SuppressWarnings("hiding")
	public TestDocument withListOfIntegers(List<Integer> listOfIntegers) {
		this.listOfIntegers = listOfIntegers;
		return this;
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}

	@SuppressWarnings("hiding")
	public TestDocument withLiteral(String literal) {
		this.literal = literal;
		return this;
	}

	public Integer getInteger() {
		return integer;
	}

	public void setInteger(Integer integer) {
		this.integer = integer;
	}

	@SuppressWarnings("hiding")
	public TestDocument withInteger(Integer integer) {
		this.integer = integer;
		return this;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	@SuppressWarnings("hiding")
	public TestDocument withDate(ZonedDateTime date) {
		this.date = date;
		return this;
	}

	public List<String> getListOfLiterals() {
		return listOfLiterals;
	}

	public void setListOfLiterals(List<String> listOfLiterals) {
		this.listOfLiterals = listOfLiterals;
	}

	@SuppressWarnings("hiding")
	public TestDocument withListOfLiterals(List<String> listOfLiterals) {
		this.listOfLiterals = listOfLiterals;
		return this;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public static void main(String[] args) throws Exception {
		TestDocument testDoc = new TestDocument().withInteger(1).withLiteral("test");
		AddDocument addDoc = AddDocument.withRandomId().withPojo(testDoc);
		System.out.println(addDoc.toJsonString());
	}

}
