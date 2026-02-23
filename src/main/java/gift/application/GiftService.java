package gift.application;

import gift.error.BusinessException;
import gift.error.CommonErrorCode;
import gift.error.GiftErrorCode;
import gift.model.Gift;
import gift.model.GiftDelivery;
import gift.model.MemberRepository;
import gift.model.Option;
import gift.model.OptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class GiftService {
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final GiftDelivery giftDelivery;

    public GiftService(
        final OptionRepository optionRepository,
        final MemberRepository memberRepository,
        final GiftDelivery giftDelivery
    ) {
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.giftDelivery = giftDelivery;
    }

    public void give(final GiveGiftRequest request, final Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.MEMBER_NOT_FOUND));
        final Option option = optionRepository.findById(request.getOptionId())
                .orElseThrow(() -> new BusinessException(GiftErrorCode.OPTION_NOT_FOUND));
        option.decrease(request.getQuantity());
        final Gift gift = new Gift(
            memberId,
            request.getReceiverId(),
            option,
            request.getQuantity(),
            request.getMessage()
        );
        giftDelivery.deliver(gift);
    }
}
