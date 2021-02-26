#pragma once

//the point of this class is to keep it to one file, the goal was to determine the optimized outlook of using just the .h file or both .h and .cpp
//the uniform grid is designed in contribution to Advanced C++/Graphics Tutorial 41: Spatial Partition Pt. 1 & 2 by MakingGamesWithBen
//the outlook discovered is that the proper way to handle classes is to indeed seperate into .h and .cpp to clean the overview of the code.

struct Cell {
	std::vector<GameObject*> balls;
};

class UniformGrid 
{
	friend class RigidBodyComponent;
	friend class CircleCollideComponent;

	//defining some const variables -> future will create seperate file which holds all public variables to use
	const int width = 640;
	const int height = 480;
	const double BALL_RADIUS = 16;
	static const int NUM_CELLS = 10;

	double radius = BALL_RADIUS;
	const int CELL_SIZE = (int)radius*2; //equal to the ball diameter
	int num_cells_x = width / CELL_SIZE; //number of cells is based on the window width to make sure all areas of the screen are covered 
	int num_cells_y = height / CELL_SIZE; //number of cells is based on the window height to make sure all areas of the screen are covered 

	std::vector<Cell> cells;

public:

	UniformGrid() 
	{ 
		SDL_Log("UniformGrid::Init");
		cells.resize(num_cells_y * num_cells_x); 
	}

	virtual ~UniformGrid() {}

	//Gets cell from cell coordinates
	Cell* getCell(int x, int y) {
		if (x < 0) x = 0;
		if (x >= num_cells_x) x = num_cells_x;
		if (y < 0) y = 0;
		if (y >= num_cells_y) y = num_cells_y;

		return &cells[y * num_cells_x + x];
	}

	//Gets cell from window coordinates
	Cell* getCell(const Vector2D& position) {
		int cellX = (int)(position.x / CELL_SIZE);
		int cellY = (int)(position.y / CELL_SIZE);

		return getCell(cellX, cellY);
	}

	//adds ball to cell
	void addBall(GameObject* ball) {
		Cell* cell = getCell(ball->position);
		cell->balls.push_back(ball);
		ball->currentCell = cell;
		ball->cellIndex = cell->balls.size() - 1;
	}

	//adds and determines ball and cell
	void addBall(GameObject* ball, Cell* cell) {
		cell->balls.push_back(ball);
		ball->currentCell = cell;
		ball->cellIndex = cell->balls.size() - 1;
	}
	
	void removeBall(GameObject* ball) {
		std::vector<GameObject*>& balls = ball->currentCell->balls;

		//normal swap
		balls[ball->cellIndex] = balls.back();
		balls.pop_back();

		//update index
		if (ball->cellIndex < balls.size()) {
			balls[ball->cellIndex]->cellIndex = ball->cellIndex;
		}

		//set index to -1
		ball->cellIndex = -1;
		ball->currentCell = nullptr;
	}

	//checked in rigidbody component as the balls move from cell to cell.
	virtual void CheckMovedCell(GameObject* ball) {
		Cell* newCell = this->getCell(ball->position);
		if (newCell != ball->currentCell) {
			this->removeBall(ball);
			this->addBall(ball, newCell);
		}
	}

	//performs the grid collision with checking neighbors
	void updateCollision()
	{
		for (int i = 0; i < cells.size(); i++) {
			int x = i % num_cells_x;
			int y = i / num_cells_x;

			Cell& cell = cells[i];

			//loop through balls in cell
			for (int j = 0; j < cell.balls.size(); j++) {

				//update the residing cell, balls and balls to check and index to start at
				checkCollision(cell.balls[j], cell.balls, j + 1);

				//update the neighbours
				if (x > 0) {
					//left
					checkCollision(cell.balls[j], getCell(x - 1, y)->balls, 0);
					if (y > 0) {
						//top left
						checkCollision(cell.balls[j], getCell(x - 1, y - 1)->balls, 0);
					}
					if (y < num_cells_y - 1) {
						//bottom left
						checkCollision(cell.balls[j], getCell(x - 1, y + 1)->balls, 0);
					}
				}
				//up cell
				if (y > 0) {
					checkCollision(cell.balls[j], getCell(x, y - 1)->balls, 0);
				}
			}
		}
	}

	//actually tests the collision with a neighboring list of balls
	void checkCollision(GameObject* ball, std::vector<GameObject*>& balls_to_check, int index)
	{
		CircleCollideComponent* circleCollider = ball->GetComponent<CircleCollideComponent*>();

		for (int i = index; i < balls_to_check.size(); i++) {
			circleCollider->checkCollision(balls_to_check[i], ball);
		}
	}

	virtual void Destroy() {
		cells.clear();
	}
};
