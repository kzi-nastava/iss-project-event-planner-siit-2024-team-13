package com.iss.eventorium.event.services;

import com.iss.eventorium.category.mappers.CategoryMapper;
import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.category.services.CategoryService;
import com.iss.eventorium.event.dtos.budget.*;
import com.iss.eventorium.event.exceptions.AlreadyProcessedException;
import com.iss.eventorium.event.mappers.BudgetMapper;
import com.iss.eventorium.event.models.Budget;
import com.iss.eventorium.event.models.BudgetItem;
import com.iss.eventorium.event.models.BudgetItemStatus;
import com.iss.eventorium.event.models.Event;
import com.iss.eventorium.event.repositories.BudgetItemRepository;
import com.iss.eventorium.event.repositories.EventRepository;
import com.iss.eventorium.event.specifications.BudgetSpecification;
import com.iss.eventorium.shared.exceptions.InsufficientFundsException;
import com.iss.eventorium.solution.dtos.products.ProductResponseDto;
import com.iss.eventorium.solution.dtos.products.SolutionReviewResponseDto;
import com.iss.eventorium.solution.mappers.ProductMapper;
import com.iss.eventorium.solution.mappers.SolutionMapper;
import com.iss.eventorium.solution.models.*;
import com.iss.eventorium.solution.services.HistoryService;
import com.iss.eventorium.solution.services.ProductService;
import com.iss.eventorium.solution.services.SolutionService;
import com.iss.eventorium.user.models.User;
import com.iss.eventorium.user.services.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class BudgetService {

    private final SolutionService solutionService;
    private final ProductService productService;
    private final EventService eventService;
    private final AuthService authService;
    private final CategoryService categoryService;
    private final HistoryService historyService;

    private final EventRepository eventRepository;
    private final BudgetItemRepository budgetItemRepository;

    private final BudgetMapper mapper;
    private final ProductMapper productMapper;
    private final SolutionMapper solutionMapper;
    private final CategoryMapper categoryMapper;

    public ProductResponseDto purchaseProduct(Long eventId, BudgetItemRequestDto request) {
        Product product = productService.find(request.getItemId());
        double netPrice = calculateNetPrice(product);
        if(netPrice > request.getPlannedAmount()) {
            throw new InsufficientFundsException("You do not have enough funds for this purchase!");
        }

        Event event = eventService.find(eventId);
        updateBudget(event, mapper.fromRequest(request, product));
        return productMapper.toResponse(product);
    }

    public List<BudgetSuggestionResponseDto> getBudgetSuggestions(Long eventId, Long categoryId, double price) {
        Category category = categoryService.find(categoryId);
        Event event = eventService.find(eventId);
        List<Solution> solutions = solutionService.findSuggestions(category, price, event.getDate());
        return solutions.stream().map(mapper::toSuggestionResponse).toList();
    }

    public BudgetResponseDto getBudget(Long eventId) {
        Event event = eventService.find(eventId);
        Budget budget = event.getBudget();
        return mapper.toResponse(budget);
    }

    public List<SolutionReviewResponseDto> getAllBudgetItems() {
        User user = authService.getCurrentUser();
        Specification<BudgetItem> specification = BudgetSpecification.filterAllBudgetItems(user);
        List<BudgetItem> items = budgetItemRepository.findAll(specification);
        return removeDuplicateItems(items).stream()
                .map(item -> solutionMapper.toReviewResponse(user, item.getSolution(), item.getItemType()))
                .toList();
    }

    public void addReservationAsBudgetItem(Reservation reservation, double plannedAmount) {
        Budget budget = reservation.getEvent().getBudget();
        Service service = reservation.getService();
        boolean isAutomatic = service.getType() == ReservationType.AUTOMATIC;

        BudgetItem budgetItem = getSolutionFromBudget(budget, service).orElseGet(() -> {
            BudgetItem item = BudgetItem.builder()
                    .itemType(SolutionType.SERVICE)
                    .plannedAmount(plannedAmount)
                    .status(isAutomatic ? BudgetItemStatus.PROCESSED : BudgetItemStatus.PENDING)
                    .processedAt(isAutomatic ? LocalDateTime.now() : null)
                    .solution(service)
                    .category(service.getCategory())
                    .build();
            budget.addItem(item);
            return item;
        });

        budgetItem.setStatus(isAutomatic ? BudgetItemStatus.PROCESSED : BudgetItemStatus.PENDING);
        budgetItem.setProcessedAt(isAutomatic ? LocalDateTime.now() : null);
        budgetItem.setPlannedAmount(plannedAmount);

        eventRepository.save(reservation.getEvent());
    }

    public void markAsReserved(Reservation reservation) {
        Event event = reservation.getEvent();
        Budget budget = event.getBudget();
        Long serviceId = reservation.getService().getId();

        BudgetItem budgetItem = budget.getItems().stream()
                .filter(item -> Objects.equals(item.getSolution().getId(), serviceId)
                        && item.getItemType() == SolutionType.SERVICE)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Matching budget item not found."));

        budgetItem.setStatus(BudgetItemStatus.PROCESSED);
        budgetItem.setProcessedAt(LocalDateTime.now());
        budgetItemRepository.save(budgetItem);
    }

    public List<BudgetItemResponseDto> getBudgetItems(Long eventId) {
        Event event = eventService.find(eventId);
        List<BudgetItem> items = event.getBudget().getItems();
        for(BudgetItem item : items) {
            if(item.getProcessedAt() != null) {
                Memento memento = historyService.getValidSolution(item.getSolution().getId(), item.getProcessedAt());
                item.getSolution().restore(memento);
            }
        }
        return items.stream().map(mapper::toResponse).toList();
    }

    public BudgetItemResponseDto createBudgetItem(Long eventId, BudgetItemRequestDto request) {
        Event event = eventService.find(eventId);
        Budget budget = event.getBudget();

        BudgetItem item = budget.getItems().stream()
                .filter(bi -> Objects.equals(bi.getSolution().getId(), request.getItemId()))
                .findFirst()
                .map(existingItem -> {
                    if(existingItem.getProcessedAt() != null)
                        throw new AlreadyProcessedException("Solution is already processed");

                    existingItem.setPlannedAmount(request.getPlannedAmount());
                    return existingItem;
                })
                .orElseGet(() -> {
                    Solution solution = solutionService.find(request.getItemId());
                    if(request.getPlannedAmount() < calculateNetPrice(solution))
                        throw new InsufficientFundsException("You didn't plan to invest this much money.");

                    BudgetItem newItem = mapper.fromRequest(request, solution);
                    newItem.setId(0L);
                    newItem.setProcessedAt(null);
                    newItem.setStatus(BudgetItemStatus.PLANNED);
                    budget.addItem(newItem);
                    return newItem;
                });
        eventRepository.save(event);
        return mapper.toResponse(item);
    }

    public BudgetItemResponseDto updateBudgetItem(Long eventId, Long itemId, UpdateBudgetItemRequestDto request) {
        Event event = eventService.find(eventId);
        Budget budget = event.getBudget();
        BudgetItem item = getFromBudget(budget, itemId);

        if(item.getProcessedAt() != null)
            throw new AlreadyProcessedException("Solution is already processed");

        if(request.getPlannedAmount() < calculateNetPrice(item.getSolution()))
            throw new InsufficientFundsException("You do not have enough funds for this purchase/reservation!");

        item.setPlannedAmount(request.getPlannedAmount());
        eventRepository.save(event);
        return mapper.toResponse(item);
    }

    public void deleteBudgetItem(Long eventId, Long itemId) {
        Event event = eventService.find(eventId);
        Budget budget = event.getBudget();
        BudgetItem item = getFromBudget(budget, itemId);
        if(!item.getStatus().equals(BudgetItemStatus.PLANNED))
            throw new AlreadyProcessedException("Solution is already processed");

        budget.removeItem(item);
        eventRepository.save(event);
    }

    private void updateBudget(Event event, BudgetItem item) {
        Budget budget = event.getBudget();
        BudgetItem budgetItem = getFromBudget(budget, item);

        budgetItem.setProcessedAt(LocalDateTime.now());
        budgetItem.setStatus(BudgetItemStatus.PROCESSED);
        eventRepository.save(event);
    }

    private Collection<BudgetItem> removeDuplicateItems(List<BudgetItem> items) {
        HashMap<Long, BudgetItem> uniqueItems = new HashMap<>();
        for(BudgetItem item : items)
            uniqueItems.put(item.getSolution().getId(), item);
        return uniqueItems.values();
    }

    private BudgetItem getFromBudget(Budget budget, BudgetItem item) {
        return budget.getItems().stream().filter(bi ->
                bi.getSolution().getId().equals(item.getSolution().getId()))
                .findFirst()
                .map(bi -> {
                    if (bi.getProcessedAt() != null)
                        throw new AlreadyProcessedException("Solution is already processed");
                    return bi;
                })
                .orElseGet(() -> {
                    budget.addItem(item);
                    return item;
                });
    }

    private BudgetItem getFromBudget(Budget budget, Long itemId) {
        return budget.getItems().stream()
                .filter(existingItem -> Objects.equals(existingItem.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Budget item not found."));
    }

    private Optional<BudgetItem> getSolutionFromBudget(Budget budget, Solution solution) {
        return budget.getItems().stream().filter(bi ->
                        bi.getSolution().getId().equals(solution.getId()))
                .findFirst()
                .map(bi -> {
                    if (bi.getProcessedAt() != null)
                        throw new AlreadyProcessedException("Solution is already processed");
                    return bi;
                });
    }

    private double calculateNetPrice(Solution solution) {
        return solution.getPrice() * (1 - solution.getDiscount() / 100);
    }

    public BudgetResponseDto updateBudgetActiveCategories(Long eventId, List<Long> categoryIds) {
        Event event = eventService.find(eventId);
        Budget budget = event.getBudget();
        List<Category> categories = categoryIds.stream().map(categoryService::find).toList();
        budget.setActiveCategories(new ArrayList<>(categories));
        eventRepository.save(event);
        return mapper.toResponse(budget);
    }
}
