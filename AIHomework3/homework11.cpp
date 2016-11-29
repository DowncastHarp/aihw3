// homework11.cpp : 
//
#include <cassert>
#include <ctime>
#include <iostream>
#include <fstream>
#include <stack>
#include <string>

#include "homework11.h"

int main()
{
	std::cout << "Hello World!" << std::endl;

	std::string inputFilename = "input.txt";
	std::string outputFilename = "output.txt";

	//std::string		algorithm;
	//int				numLivePaths;
	//int				numSundayPaths;

	//Read the input file
	std::ifstream inputFile(inputFilename);
	if (inputFile.is_open())
	//{
	//	// Determine which algorithm to use
	//	std::getline(inputFile, algorithm);
	//	if (algorithm == "BFS")
	//	{
	//		qFunc = new bfs;
	//	}
	//	else if (algorithm == "DFS")
	//	{
	//		qFunc = new dfs;
	//	}
	//	else if (algorithm == "UCS")
	//	{
	//		qFunc = new ucs;
	//	}
	//	else if (algorithm == "A*")
	//	{
	//		qFunc = new astar;
	//	}
	//	else
	//	{
	//		std::cout << "ERROR: Unrecognized algorithm";
	//		return 0;
	//	}

	//	std::getline(inputFile, prob.m_start);
	//	std::getline(inputFile, prob.m_goal);

	//	//Build the graph of live paths
	//	std::string numLivePathsStr;
	//	std::getline(inputFile, numLivePathsStr);
	//	numLivePaths = std::stoi(numLivePathsStr);
	//	for (int i = 0; i < numLivePaths; ++i)
	//	{
	//		std::string startState, childState, pathCostStr;
	//		std::getline(inputFile, startState, ' ');
	//		// Second bit is the child's state
	//		std::getline(inputFile, childState, ' ');
	//		// Remaining bit is the path cost
	//		std::getline(inputFile, pathCostStr);
	//		int pathCost = std::stoi(pathCostStr);

	//		if (prob.m_graph.find(startState) != prob.m_graph.end())
	//		{
	//			// The node already exists
	//			Node &node = *GetNodeByState(startState);

	//			// Check if the child already exists in the graph, add if it doesn't
	//			if (prob.m_graph.find(childState) == prob.m_graph.end())
	//			{
	//				Node* childNode = new Node();
	//				childNode->m_state = childState;

	//				prob.m_graph.insert(std::make_pair(childState, childNode));
	//			}

	//			node.m_children.push_back(std::pair<std::string, int>(childState, pathCost));
	//		}
	//		else
	//		{
	//			// The node doesn't already exist
	//			Node* node = new Node();
	//			node->m_state = startState;

	//			// Check if the child already exists in the graph, add if it doesn't
	//			if (prob.m_graph.find(childState) == prob.m_graph.end())
	//			{
	//				Node* childNode = new Node();
	//				childNode->m_state = childState;

	//				prob.m_graph.insert(std::make_pair(childState, childNode));
	//			}

	//			node->m_children.push_back(std::pair<std::string, int>(childState, pathCost));

	//			prob.m_graph.insert(std::make_pair(startState, node));
	//		}
	//	}

	//	//Add the Sunday traffic heuristic to the nodes
	//	std::string numSundayPathsStr;
	//	std::getline(inputFile, numSundayPathsStr);
	//	numSundayPaths = std::stoi(numSundayPathsStr);
	//	for (int i = 0; i < numSundayPaths; ++i)
	//	{
	//		std::string sundayNodeState, sundayCostStr;
	//		std::getline(inputFile, sundayNodeState, ' ');
	//		std::getline(inputFile, sundayCostStr);

	//		int sundayCost = std::stoi(sundayCostStr);

	//		Node* sundayNode = GetNodeByState(sundayNodeState);
	//		if (sundayNode)
	//		{
	//			sundayNode->m_heuristic = sundayCost;
	//		}
	//	}

		inputFile.close();
	//}
	//else
	//{
	//	std::cout << "Unable to open file\n";
	//}

	clock_t start = clock();

	////do stuff here

	clock_t finish = clock();
	double runtime = (finish - start) / double(CLOCKS_PER_SEC) * 1000;
	//std::cout << algorithm << " running time: " << runtime << "ms" << std::endl;

	return 0;
}

