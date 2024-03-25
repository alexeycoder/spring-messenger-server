package edu.alexey.messengerserver.entities;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "persons")
@DynamicUpdate
@DynamicInsert
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Person implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long personId;
	private String fullName;
	private String phone;

	public Person() {
		this("", "", "");
	}

	public Person(String fullName, String phone, String address) {
		this.fullName = fullName;
		this.phone = phone;
		this.address = address;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "person_id")
	@Basic
	@EqualsAndHashCode.Include
	@ToString.Include
	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	@Column(name = "full_name")
	@Basic
	@EqualsAndHashCode.Include
	@ToString.Include
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Column(name = "phone")
	@Basic
	@EqualsAndHashCode.Include
	@ToString.Include
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "photo")
	@Basic
	@Getter
	@Setter
	private byte[] photo;

	@Column(name = "address")
	@Basic
	@Getter
	@Setter
	private String address;

	//	@Override
	//	public String toString() {
	//		return String.format("Person [fullName=%s, phone=%s]", getFullName(), getPhone());
	//	}

}
