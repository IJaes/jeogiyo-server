package com.ijaes.jeogiyo.review.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ijaes.jeogiyo.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "j_review")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
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

	//리뷰 숨김처리 여부
	@Column(nullable = false)
	private boolean isHidden = false;

	//삭제 여부
	@Column(nullable = false)
	private Boolean isDeleted = false;

	@Column
	private LocalDateTime deletedAt;

	//제목 업데이트
	public void updateTitle(String title) {
		this.title = title;
	}

	//내용 업데이트
	public void updateContent(String content) {
		this.content = content;
	}

	//평점 업데이트
	public void updateRate(Integer rate) {
		this.rate = rate;
	}

	//리뷰 숨김 처리
	public void hide() {
		this.isHidden = true;
	}

	//리뷰 숨김 해제 처리
	public void show() {
		this.isHidden = false;
	}

	public void softDelete() {
		this.isDeleted = true;
		this.deletedAt = LocalDateTime.now();
	}
}
