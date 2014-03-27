package uk.co.bssd.hank.test.collection;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import uk.co.bssd.hank.collection.AttributeSelector;
import uk.co.bssd.hank.collection.BaseCollection;

public class BaseCollectionTest {

	public static class ValueObject {
		private final String id;
		private final String nonUniqueField;
		private final String nullableField;

		public static ValueObject create(String id, String nonUniqueField, String nullableField) {
			return new ValueObject(id, nonUniqueField, nullableField);
		}

		private ValueObject(String id, String nonUniqueField, String nullableField) {
			this.id = id;
			this.nonUniqueField = nonUniqueField;
			this.nullableField = nullableField;
		}

		public String id() {
			return this.id;
		}

		public String nonUniqueField() {
			return this.nonUniqueField;
		}
		
		public String nullableField() {
			return this.nullableField;
		}
	}
	
	public static class IdSelector implements AttributeSelector<ValueObject> {
		@Override
		public String value(ValueObject instance) {
			return instance.id();
		}
	}
	
	public static class NonUniqueFieldSelector implements AttributeSelector<ValueObject> {
		@Override
		public String value(ValueObject instance) {
			return instance.nonUniqueField();
		}
	}
	
	public static class NullableFieldSelector implements AttributeSelector<ValueObject> {
		@Override
		public String value(ValueObject instance) {
			return instance.nullableField();
		}
	}
	
	public static AttributeSelector<ValueObject> idSelector() {
		return new IdSelector();
	}
	
	public static AttributeSelector<ValueObject> nonUniqueFieldSelector() {
		return new NonUniqueFieldSelector();
	}
	
	public static AttributeSelector<ValueObject> nullableFieldSelector() {
		return new NullableFieldSelector();
	}

	public static class ValueObjects extends BaseCollection<ValueObjects, ValueObject> {

		public static ValueObjects over(ValueObject... valueObjects) {
			return new ValueObjects(Arrays.asList(valueObjects));
		}

		private ValueObjects(List<ValueObject> collection) {
			super(collection);
		}
		
		protected ValueObjects newCollection(List<ValueObject> collection) {
			return new ValueObjects(collection);
		}

		public ValueObject findById(String id) {
			return find(idSelector(), id);
		}

		public ValueObjects listWithNonUniqueField(String nonUniqueField) {
			return list(nonUniqueFieldSelector(), nonUniqueField);
		}
		
		public ValueObjects listWithNullableField(String nullableField) {
			return list(nullableFieldSelector(), nullableField);
		}
	}

	private static final String NON_UNIQUE_VALUE = "Notes";
	private static final String NOT_NULL_VALUE = "Not Null";

	private static final ValueObject VALUE_OBJECT_1 = ValueObject.create(
			newId(), "Unique", NOT_NULL_VALUE);
	private static final ValueObject VALUE_OBJECT_2 = ValueObject.create(
			newId(), NON_UNIQUE_VALUE, null);
	private static final ValueObject VALUE_OBJECT_3 = ValueObject.create(
			newId(), NON_UNIQUE_VALUE, NOT_NULL_VALUE);

	private ValueObjects valueObjects;

	@Before
	public void before() {
		this.valueObjects = ValueObjects.over(VALUE_OBJECT_1, VALUE_OBJECT_2,
				VALUE_OBJECT_3);
	}

	@Test
	public void testSize() {
		assertThat(this.valueObjects.size(), is(3));
	}

	@Test
	public void testIterable() {
		Iterator<ValueObject> iterator = this.valueObjects.iterator();
		assertThat(iterator.next(), is(VALUE_OBJECT_1));
		assertThat(iterator.next(), is(VALUE_OBJECT_2));
		assertThat(iterator.next(), is(VALUE_OBJECT_3));
	}

	@Test
	public void testFindIndividualObjectWhenItExists() {
		assertThat(this.valueObjects.findById(VALUE_OBJECT_2.id()),
				is(VALUE_OBJECT_2));
	}

	@Test(expected = IllegalStateException.class)
	public void testFindIndividualObjectThrowsExceptionWhenItDoesNotExist() {
		this.valueObjects.findById(newId());
	}

	@Test
	public void testListObjectsByValueThatExists() {
		ValueObjects filteredObjects = this.valueObjects
				.listWithNonUniqueField(NON_UNIQUE_VALUE);

		assertThat(filteredObjects.size(), is(2));
		assertThat(filteredObjects, hasItem(VALUE_OBJECT_2));
		assertThat(filteredObjects, hasItem(VALUE_OBJECT_3));
	}
	
	@Test
	public void testListingObjectsByValuesThatArePotentiallyNull() {
		ValueObjects filteredObjects = this.valueObjects.listWithNullableField(NOT_NULL_VALUE);
		
		assertThat(filteredObjects.size(), is(2));
		assertThat(filteredObjects, hasItem(VALUE_OBJECT_1));
		assertThat(filteredObjects, hasItem(VALUE_OBJECT_3));		
	}
	
	@Test
	public void testListingObjectsLookingForNullValues() {
		ValueObjects filteredObjects = this.valueObjects.listWithNullableField(null);
		
		assertThat(filteredObjects.size(), is(1));
		assertThat(filteredObjects, hasItem(VALUE_OBJECT_2));
	}

	private static String newId() {
		return UUID.randomUUID().toString();
	}
}