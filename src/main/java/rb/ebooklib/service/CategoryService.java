package rb.ebooklib.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rb.ebooklib.model.Category;
import rb.ebooklib.persistence.BookRepository;
import rb.ebooklib.persistence.BookSpecifications;
import rb.ebooklib.persistence.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

import static rb.ebooklib.util.NullOrEmptyUtil.isNullOrEmpty;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BookRepository bookRepository;

    /**
     * Database call to retrieve all categories
     *
     * @return list of existing Categories
     */
    public List<Category> getAllCategories() {
        return this.categoryRepository.findAllByOrderByName();
    }

    /**
     * Check if all the categories from the list exist in the database.
     * If a category does not exist, it will be added to the database.
     *
     * @param newCategories a list of categories saved with the book
     * @return list of categories with id's that are added or updated with the book.
     */
    @Transactional
    List<Category> mergeNewCategories(List<Category> newCategories) {
/*
        List<Category> categoryList = new ArrayList<>();
        for (Category category: newCategories) {
            if (!isNullOrEmpty(category.getName())) {
                // Optional<Category> currentCategory = categoryRepository.findOneByName(category.getName());
                if (!categoryRepository.existsById(category.getId())) {
                    categoryRepository.save(category);
                }
                categoryList.add(category);
            }
        }

        return categoryList;
*/


        List<Category> collect = newCategories.stream().filter(category -> !isNullOrEmpty(category.getName()))
                .map(category -> categoryRepository.findOneByName(category.getName())
                        .orElseGet(() -> categoryRepository.save(category)))
                .collect(Collectors.toList());
        return collect;
    }

    /**
     * Check if a category still is being used by a book.
     * If a category is not being used anymore, it will be removed from the database.
     *
     * ( This function is necessary since a @ManyToMany relationship with cascadetype ALL
     *   will also remove categories still being used when a book is removed )
     *
     * @param categoryId The category to be removed if orphan
     */
    @Transactional
    public void removeCategoryWhenOrphan(final Long categoryId) {
        if (bookRepository.count(BookSpecifications.bookHasCategoryId(categoryId)) == 0) {
            categoryRepository.findById(categoryId).ifPresent(category -> categoryRepository.deleteById(category.getId()));
        }
    }

}
