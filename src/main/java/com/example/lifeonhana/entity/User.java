package com.example.lifeonhana.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter @Setter
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 100)
	private String authId;

	@Column(length = 100)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;

	@Column(nullable = false)
	private String providerId;

	@Column(nullable = false)
	private String birthday;

	@Column(nullable = false)
	private Boolean isFirst;

	@OneToMany(mappedBy = "user")
	private List<ArticleLike> articleLikes = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<ProductLike> productLikes = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<History> histories = new ArrayList<>();

	@OneToMany(mappedBy = "user")
	private List<LumpSum> lumpSums = new ArrayList<>();

	@OneToOne(mappedBy = "user")
	private Mydata mydata;

	@OneToMany(mappedBy = "user")
	private List<Wallet> wallets = new ArrayList<>();

	public enum Provider {
		KAKAO, GOOGLE, NAVER
	}
}
