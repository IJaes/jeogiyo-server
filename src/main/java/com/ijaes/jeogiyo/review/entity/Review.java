package com.ijaes.jeogiyo.review.entity;

import com.ijaes.jeogiyo.common.entity.BaseEntity;
import com.ijaes.jeogiyo.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "j_review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Review extends BaseEntity {

	// 리뷰 식별 ID
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private Long reviewId;

	// 작성자 ID
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	// 가게 ID
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	//리뷰 제목
	@Column(nullable = false)
	private String title;

	// 리뷰 내용
	@Column(nullable = false, length = 500) // 리뷰 내용 길이 제한
	private String content;

	// 평점
	@Column(nullable = false)
	private Integer rate;
}
