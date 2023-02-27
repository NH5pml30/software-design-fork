package ru.akirakozov.sd.app.account.model;

import ru.akirakozov.sd.app.shared.model.Share;

public interface ShareInfoGetter {
    Share getShareInfo(int shareId);
}
