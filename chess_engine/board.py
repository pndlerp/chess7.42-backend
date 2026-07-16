from enums import Piece


class Board:
    def __init__(self):
        self.board = [[Piece.EMPTY for _ in range(8)] for _ in range(8)]
        self.board[0] = [
            Piece.WHITE_ROOK,
            Piece.WHITE_KNIGHT,
            Piece.WHITE_BISHOP,
            Piece.WHITE_QUEEN,
            Piece.WHITE_KING,
            Piece.WHITE_BISHOP,
            Piece.WHITE_KNIGHT,
            Piece.WHITE_ROOK,
        ]
        self.board[1] = [Piece.WHITE_PAWN] * 8
        self.board[6] = [Piece.BLACK_PAWN] * 8
        self.board[7] = [
            Piece.BLACK_ROOK,
            Piece.BLACK_KNIGHT,
            Piece.BLACK_BISHOP,
            Piece.BLACK_QUEEN,
            Piece.BLACK_KING,
            Piece.BLACK_BISHOP,
            Piece.BLACK_KNIGHT,
            Piece.BLACK_ROOK,
        ]

    def print_board(self):
        for row in self.board:
            print(" ".join(str(piece) for piece in row))
