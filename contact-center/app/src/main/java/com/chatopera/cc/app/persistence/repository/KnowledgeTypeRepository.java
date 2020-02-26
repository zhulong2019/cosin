/*
 * Copyright (C) 2017 优客服-多渠道客服系统
 * Modifications copyright (C) 2018 Chatopera Inc, <https://www.chatopera.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chatopera.cc.app.persistence.repository;

import java.util.List;

import com.chatopera.cc.app.model.KnowledgeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract interface KnowledgeTypeRepository extends
        JpaRepository<KnowledgeType, String> {

    public abstract KnowledgeType findByIdAndOrgi(String id, String orgi);

    public abstract int countByNameAndOrgiAndIdNot(String name, String orgi, String id);

    public abstract int countByNameAndOrgiAndParentidNot(String name, String orgi, String parentid);

    public abstract List<KnowledgeType> findByOrgi(String orgi);

    public abstract List<KnowledgeType> findByOrgiAndTypeid(String orgi, String typeid);

    public abstract KnowledgeType findByNameAndOrgi(String name, String orgi);

    public abstract KnowledgeType findByNameAndOrgiAndIdNot(String name, String orgi, String id);

    public Page<KnowledgeType> findAll(Specification<KnowledgeType> spec, Pageable pageable);  //分页按条件查询
}
