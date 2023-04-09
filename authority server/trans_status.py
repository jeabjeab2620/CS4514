from enum import Enum

class TransStatus(Enum):
    REQUESTED = "REQUESTED"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"

if __name__ == "__main__":
    print(TransStatus.REQUESTED.name)