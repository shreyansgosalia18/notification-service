package com.karboncard.assignment.notificationservice.service.factory;

import com.karboncard.assignment.notificationservice.model.enums.NotificationType;
import com.karboncard.assignment.notificationservice.model.enums.NotificationPriority;
import com.karboncard.assignment.notificationservice.service.provider.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NotificationProviderFactory {

    private final Map<NotificationType, NotificationProvider> providerMap;

    public NotificationProviderFactory(List<NotificationProvider> providers) {
        providerMap = new EnumMap<>(NotificationType.class);

        for (NotificationProvider provider : providers) {
            providerMap.put(provider.getType(), provider);
            log.info("Registered notification provider for type: {}", provider.getType());
        }
    }

    public NotificationProvider getProvider(NotificationType type) {
        NotificationProvider provider = providerMap.get(type);

        if (provider == null) {
            log.error("No provider found for notification type: {}", type);
            throw new IllegalArgumentException("Unsupported notification type: " + type);
        }

        return provider;
    }

    /**
     * Checks if a specific notification provider is available
     * @param type The notification type to check
     * @return true if provider exists, false otherwise
     */
    public boolean hasProvider(NotificationType type) {
        return providerMap.containsKey(type);
    }

    /**
     * Returns all registered notification providers
     * @return Map of all providers by type
     */
    public Map<NotificationType, NotificationProvider> getAllProviders() {
        return new EnumMap<>(providerMap);
    }
}