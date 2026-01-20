package com.ying.learneyjourney.service;

import com.stripe.model.Transfer;
import com.ying.learneyjourney.entity.Purchase;
import com.ying.learneyjourney.entity.StripeTransfer;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.repository.StripeTransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripeTransferService {
    private final StripeTransferRepository stripeTransferRepository;
    public void create(Transfer t, Purchase p, TutorProfile tutorProfile, UUID orderId, String status) {
        StripeTransfer stripeTransfer = new StripeTransfer();
        stripeTransfer.setPurchaseId(p.getId());
        stripeTransfer.setTutorProfileId(tutorProfile.getId());
        stripeTransfer.setStripeAccountId(tutorProfile.getStripConnect());
        stripeTransfer.setAmount(p.getAmount());
        if(t != null) stripeTransfer.setStripeTransferId(t.getId());
        stripeTransfer.setStatus(status);
        stripeTransferRepository.save(stripeTransfer);
    }

    public void update(Purchase p,Transfer t, String status) {
        StripeTransfer stripeTransfer = stripeTransferRepository.findBy_purchseId(p.getId()).orElseThrow(() -> new BusinessException("Stripe Transfer not found for purchase id: " + p.getId(), "STRIPE_TRANSFER_NOT_FOUND"));
        stripeTransfer.setStripeTransferId(t.getId());
        stripeTransfer.setStatus(status);
        stripeTransferRepository.save(stripeTransfer);
    }
}
