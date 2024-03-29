package edu.alexey.messengerserver.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "messages")
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_seq")
	@SequenceGenerator(name = "message_seq", sequenceName = "message_id_sequence", initialValue = 1, allocationSize = 20)
	@EqualsAndHashCode.Include
	@ToString.Include
	@Column(name = "message_id")
	private Long messageId;

	@EqualsAndHashCode.Include
	@ToString.Include
	@Generated(event = EventType.INSERT)
	@ColumnDefault(value = "gen_random_uuid()")
	@Column(name = "message_uuid", nullable = false, unique = true, insertable = false, updatable = false)
	private UUID messageUuid;

	@ManyToOne
	@JoinColumn(name = "sender_user_id", referencedColumnName = "user_id")
	private User sender;

	@ManyToOne
	@JoinColumn(name = "addressee_user_id", referencedColumnName = "user_id")
	private User addressee;

	@EqualsAndHashCode.Include
	@ToString.Include
//	@Generated(event = EventType.INSERT)
//	@ColumnDefault(value = "localtimestamp")
//	@Column(name = "sent_at", nullable = false, insertable = false, updatable = false)
	@Column(name = "sent_at", nullable = false, updatable = false)
	private LocalDateTime sentAt;

	@EqualsAndHashCode.Include
	@Column(name = "content", nullable = false, updatable = false)
	private String content;
}
