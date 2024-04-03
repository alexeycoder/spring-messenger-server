package edu.alexey.messengerserver.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
//@DynamicUpdate
//@DynamicInsert
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "users")
public class User implements Serializable, UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	@ToString.Include
	@Column(name = "user_id")
	private Long userId;

	//@GeneratedValue(strategy = GenerationType.UUID, generator = "user_seq")
	//@SequenceGenerator(name = "user_seq", sequenceName = "user_uuid_sequence", allocationSize = 20)
	@EqualsAndHashCode.Include
	@ToString.Include
	@Generated(event = EventType.INSERT)
	@ColumnDefault(value = "gen_random_uuid()")
	@Column(name = "user_uuid", /*columnDefinition = "UUID DEFAULT gen_random_uuid()",*/ nullable = false, unique = true, insertable = false, updatable = false)
	private UUID userUuid;

	@EqualsAndHashCode.Include
	@ToString.Include
	@Generated(event = EventType.INSERT)
	@ColumnDefault(value = "localtimestamp")
	@Column(name = "registered_at", nullable = false, insertable = false, updatable = false)
	private LocalDateTime registeredAt;

	@EqualsAndHashCode.Include
	@ToString.Include
	@Getter(onMethod = @__(@Override))
	@Column(name = "username", nullable = false, unique = true)
	private String username;

	@Getter(onMethod = @__(@Override))
	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "display_name")
	private String displayName;

	@OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
	private List<Message> messagesSent = new ArrayList<Message>();

	@OneToMany(mappedBy = "addressee", fetch = FetchType.LAZY)
	private List<Message> messagesDestined = new ArrayList<Message>();

	@Transient
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Transient
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Transient
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Transient
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Transient
	@Override
	public boolean isEnabled() {
		return true;
	}

}
