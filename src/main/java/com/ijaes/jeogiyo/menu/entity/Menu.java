package com.ijaes.jeogiyo.menu.entity;

import com.ijaes.jeogiyo.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "j_menu")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {


}
