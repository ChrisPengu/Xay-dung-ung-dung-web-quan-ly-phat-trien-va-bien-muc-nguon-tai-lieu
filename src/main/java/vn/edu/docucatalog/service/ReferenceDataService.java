package vn.edu.docucatalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.docucatalog.domain.Author;
import vn.edu.docucatalog.domain.Category;
import vn.edu.docucatalog.domain.Publisher;
import vn.edu.docucatalog.domain.Supplier;
import vn.edu.docucatalog.repository.AuthorRepository;
import vn.edu.docucatalog.repository.CategoryRepository;
import vn.edu.docucatalog.repository.PublisherRepository;
import vn.edu.docucatalog.repository.SupplierRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferenceDataService {
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final SupplierRepository supplierRepository;

    public List<Category> categories() { return categoryRepository.findAllByOrderByNameAsc(); }
    public List<Author> authors() { return authorRepository.findAllByOrderByNameAsc(); }
    public List<Publisher> publishers() { return publisherRepository.findAllByOrderByNameAsc(); }
    public List<Supplier> suppliers() { return supplierRepository.findAllByOrderByNameAsc(); }

    public Category category(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thể loại"));
    }

    public Author author(Long id) {
        return authorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác giả"));
    }

    public Publisher publisher(Long id) {
        return publisherRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xuất bản"));
    }

    public Supplier supplier(Long id) {
        return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));
    }

    @Transactional
    public Category saveCategory(Category input) {
        Long id = input.getId();
        String code = input.getCode().trim();
        boolean duplicate = id == null ? categoryRepository.existsByCodeIgnoreCase(code)
                : categoryRepository.existsByCodeIgnoreCaseAndIdNot(code, id);
        if (duplicate) throw new BusinessException("Mã thể loại đã tồn tại");
        Category target = id == null ? new Category() : category(id);
        target.setCode(code.toUpperCase());
        target.setName(input.getName().trim());
        target.setDescription(trimToNull(input.getDescription()));
        return categoryRepository.save(target);
    }

    @Transactional
    public Author saveAuthor(Author input) {
        Author target = input.getId() == null ? new Author() : author(input.getId());
        target.setName(input.getName().trim());
        target.setBirthYear(input.getBirthYear());
        target.setNationality(trimToNull(input.getNationality()));
        target.setNote(trimToNull(input.getNote()));
        return authorRepository.save(target);
    }

    @Transactional
    public Publisher savePublisher(Publisher input) {
        Long id = input.getId();
        String name = input.getName().trim();
        boolean duplicate = id == null ? publisherRepository.existsByNameIgnoreCase(name)
                : publisherRepository.existsByNameIgnoreCaseAndIdNot(name, id);
        if (duplicate) throw new BusinessException("Tên nhà xuất bản đã tồn tại");
        Publisher target = id == null ? new Publisher() : publisher(id);
        target.setName(name);
        target.setAddress(trimToNull(input.getAddress()));
        target.setEmail(trimToNull(input.getEmail()));
        target.setPhone(trimToNull(input.getPhone()));
        return publisherRepository.save(target);
    }

    @Transactional
    public Supplier saveSupplier(Supplier input) {
        Long id = input.getId();
        String name = input.getName().trim();
        boolean duplicate = id == null ? supplierRepository.existsByNameIgnoreCase(name)
                : supplierRepository.existsByNameIgnoreCaseAndIdNot(name, id);
        if (duplicate) throw new BusinessException("Tên nhà cung cấp đã tồn tại");
        Supplier target = id == null ? new Supplier() : supplier(id);
        target.setName(name);
        target.setContactPerson(trimToNull(input.getContactPerson()));
        target.setEmail(trimToNull(input.getEmail()));
        target.setPhone(trimToNull(input.getPhone()));
        target.setAddress(trimToNull(input.getAddress()));
        return supplierRepository.save(target);
    }

    @Transactional
    public void deleteCategory(Long id) { delete(() -> { categoryRepository.delete(category(id)); categoryRepository.flush(); }, "Thể loại đang được tài liệu sử dụng"); }

    @Transactional
    public void deleteAuthor(Long id) { delete(() -> { authorRepository.delete(author(id)); authorRepository.flush(); }, "Tác giả đang được tài liệu sử dụng"); }

    @Transactional
    public void deletePublisher(Long id) { delete(() -> { publisherRepository.delete(publisher(id)); publisherRepository.flush(); }, "Nhà xuất bản đang được tài liệu sử dụng"); }

    @Transactional
    public void deleteSupplier(Long id) { delete(() -> { supplierRepository.delete(supplier(id)); supplierRepository.flush(); }, "Nhà cung cấp đang được đề xuất sử dụng"); }

    private void delete(Runnable operation, String message) {
        try {
            operation.run();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(message);
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
