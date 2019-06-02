package com.example.storeregulate.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author PitterWang
 * @create 2019/5/30
 * @since 1.0.0
 */
@Service("authorService")
public class AuthorServiceImpl implements AuthorService {
	@Autowired
	private AuthorDao authorDao;

	@Override
	public int add(Author author) {
		return this.authorDao.add(author);
	}

	@Override
	public int update(Author author) {
		return this.authorDao.update(author);
	}

	@Override
	public int delete(Long id) {
		return this.authorDao.delete(id);
	}

	@Override
	public Author findAuthor(Long id) {
		return this.authorDao.findAuthor(id);
	}

	@Override
	public List<Author> findAuthorList() {
		return this.authorDao.findAuthorList();
	}
}