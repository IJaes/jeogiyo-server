package com.ijaes.jeogiyo.review.domain;

import java.util.UUID;

import com.ijaes.jeogiyo.common.entity.BaseEntity;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Access(AccessType.FIELD)
public class Review extends BaseEntity {

	// 리뷰 식별 ID
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID reviewId;

	//주문 내역 식별 ID
	@Column(nullable = false)
	private UUID orderId;

	// 작성자 ID
	@Column(nullable = false)
	private UUID userId;

	// 가게 ID
	@Column(nullable = false)
	private UUID storeId;

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
