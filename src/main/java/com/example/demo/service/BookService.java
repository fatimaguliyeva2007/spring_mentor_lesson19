package com.example.demo.service;

import com.example.demo.Book;
import com.example.demo.entity.BookEntity;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.InvalidBookDataException;
import com.example.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(this::toBook)
                .toList();
    }

    public Book findById(Integer id) {
        BookEntity entity = bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookNotFoundException(
                                "Book not found with id: " + id));

        return toBook(entity);
    }

    public List<Book> searchByGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            return findAll();
        }

        return bookRepository.findAll()
                .stream()
                .filter(book -> book.getGenre() != null
                        && book.getGenre().equalsIgnoreCase(genre))
                .map(this::toBook)
                .toList();
    }

    public Book saveBook(Book book) {
        validateBook(book);

        BookEntity entity = new BookEntity(
                null,
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getStock(),
                book.getPrice()
        );

        return toBook(bookRepository.save(entity));
    }

    public Book update(Integer id, Book book) {
        validateBook(book);

        BookEntity entity = bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookNotFoundException(
                                "Book not found with id: " + id));

        entity.setTitle(book.getTitle());
        entity.setAuthor(book.getAuthor());
        entity.setGenre(book.getGenre());
        entity.setStock(book.getStock());
        entity.setPrice(book.getPrice());

        return toBook(bookRepository.save(entity));
    }

    public void deleteById(Integer id) {
        BookEntity entity = bookRepository.findById(id)
                .orElseThrow(() ->
                        new BookNotFoundException("Book not found with id: " + id));

        bookRepository.delete(entity);
    }

    private void validateBook(Book book) {
        if (book == null) {
            throw new InvalidBookDataException("Book data is required");
        }

        if (book.getPrice() == null || book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBookDataException("Price cannot be negative or null");
        }

        if (book.getStock() == null || book.getStock() < 0) {
            throw new InvalidBookDataException("Stock cannot be negative or null");
        }
    }

    public Book patch(Integer id, Book book) {
        BookEntity entity = bookRepository.findById(id).orElseThrow(() ->
                        new BookNotFoundException("Book not found with id: " + id));

        if (book.getPrice() != null && book.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBookDataException("Price cannot be negative");
        }

        if (book.getStock() != null && book.getStock() < 0) {
            throw new InvalidBookDataException("Stock cannot be negative");
        }

        if (book.getTitle() != null) {entity.setTitle(book.getTitle());}
        if (book.getAuthor() != null) {entity.setAuthor(book.getAuthor());}
        if (book.getGenre() != null) {entity.setGenre(book.getGenre());}
        if (book.getStock() != null) {entity.setStock(book.getStock());}
        if (book.getPrice() != null) {entity.setPrice(book.getPrice());}

        return toBook(bookRepository.save(entity));
    }

    private Book toBook(BookEntity entity) {
        return new Book(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getGenre(),
                entity.getStock(),
                entity.getPrice()
        );
    }
}